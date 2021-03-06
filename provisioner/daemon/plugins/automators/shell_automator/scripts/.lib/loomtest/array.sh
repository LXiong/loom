#!/usr/bin/env bash
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

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. $DIR/_expect_success.sh simple_array "foo bar"
. $DIR/_expect_success.sh nested_array "a b c"
. $DIR/_expect_failure.sh nonunique_array
. $DIR/_expect_success.sh ^/nonunique_array "a b"
. $DIR/_expect_success.sh /config/cluster/nonunique_array "name conflict"
. $DIR/_expect_success.sh array_spaces
