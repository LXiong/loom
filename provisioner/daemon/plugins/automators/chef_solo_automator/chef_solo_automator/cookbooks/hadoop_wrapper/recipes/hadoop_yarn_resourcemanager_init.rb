#
# Cookbook Name:: hadoop_wrapper
# Recipe:: hadoop_yarn_resourcemanager_init
#
# Copyright (C) 2013 Continuuity, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

include_recipe 'hadoop_wrapper::default'
include_recipe 'hadoop::hadoop_yarn_resourcemanager'

dfs = node['hadoop']['core_site']['fs.defaultFS']
ruby_block 'initaction-create-hdfs-tmpdir' do
  block do
    resources('execute[hdfs-tmpdir]').run_action(:run)
  end
  not_if "hdfs dfs -ls #{dfs} | grep ' /tmp' | grep -e '^drwxrwxrwt'", :user => 'hdfs'
end
