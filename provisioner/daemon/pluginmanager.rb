#
# Copyright 2012-2014, Continuuity, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
require 'json'
require_relative 'automator'
require_relative 'provider'

class PluginManager
  attr_accessor :providermap, :automatormap, :tasks
  def initialize()
    @providermap = Hash.new{ |h,k| h[k] = Hash.new(&h.default_proc) }
    @automatormap = Hash.new{ |h,k| h[k] = Hash.new(&h.default_proc) }
    scanPlugins()
  end

  # scan plugins directory for json plugin definitions, load and register plugins 
  def scanPlugins
    # enforces directory structure from top-level: ./plugins/['providers']/[plugin-name]/*.json
    Dir["#{File.expand_path(File.dirname(__FILE__))}/plugins/*/*/*.json"].each do |jsonfile| 
      log.debug "pluginmanager loading #{jsonfile}"
      jsondata =  JSON.parse( IO.read(jsonfile) )
      ### New
      # { name: name, type: type, classname: class }
      if (jsondata.has_key? 'classname')
        log.debug "loading plugin class: #{File.dirname(jsonfile)}/#{jsondata['classname']}"
        # require every .rb file in the plugin top-level directory
        Dir["#{File.dirname(jsonfile)}/*.rb"].each {|file| require file }
        # check ancestor to determine plugin type
        klass = Object.const_get(jsondata['classname'])
        if klass.ancestors.include? Object.const_get('Provider')
          ptype = "provider"
        elsif klass.ancestors.include? Object.const_get('Automator')
          ptype = "automator"
        else
          ptype = "unknown"
        end
        # Defined type
        if (jsondata.has_key? 'type' && jsondata['type'] == ptype)
          case ptype
          when 'automator'
            @automatormap.merge!({jsondata['name'] => jsondata})
          when 'provider'
            @providermap.merge!({jsondata['name'] => jsondata})
          when 'unknown'
            log.error "Unknown plugin type for plugin: #{jsonfile}"
            next
          end
          log.info "registered #{ptype} plugin: #{jsondata['name']}"
        else
          log.error "Plugin type #{jsondata['type']} does not match extended class type #{ptype} in #{jsonfile}"
        end
      end
    end
  end
   
  # returns registered class name for given provider plugin
  def getHandlerActionObjectForProvider(providerName)
    if @providermap.has_key?(providerName) 
      if @providermap[providerName].has_key?('classname')
        return @providermap[providerName]['classname']
      end
    end
    raise "No registered provider for #{providerName}"
  end

  # returns registered class name for given automator plugin
  def getHandlerActionObjectForAutomator(automatorName)
    if @automatormap.has_key?(automatorName) 
      if @automatormap[automatorName].has_key?('classname')
        return @automatormap[automatorName]['classname']
      end
    end
    raise "No registered automator for #{automatorName}"
  end

  # returns all registered automators, used for bootstrap task
  def getAllHandlerActionObjectsForAutomators
    results = Array.new
    @automatormap.each do |k, v|
      if (v.has_key?('classname')) 
        results.push(v['classname'])
      end
    end
    results
  end

end
    

