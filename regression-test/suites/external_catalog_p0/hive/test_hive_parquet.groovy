// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

suite("test_hive_parquet", "p0") {
    def q01 = {
        qt_q01 """
        select * from partition_table order by l_orderkey;
    """
    }

    def q02 = {
        qt_q02 """
        select count(*) from partition_table;
    """
    }

    def q03 = {
        qt_q03 """
        select count(city) from partition_table;
    """
    }

    def q04 = {
        qt_q04 """
        select count(nation) from partition_table;
    """
    }

    def q05 = {
        qt_q05 """
        select distinct city from partition_table order by city;
    """
    }

    def q06 = {
        qt_q06 """
        select distinct nation from partition_table order by nation;
    """
    }

    def q07 = {
        qt_q07 """
        select city from partition_table order by city limit 10;
    """
    }

    def q08 = {
        qt_q08 """
        select nation from partition_table order by nation limit 10;
    """
    }

    def q09 = {
        qt_q09 """
        select city, nation from partition_table order by nation, city limit 10;
    """
    }

    def q10 = {
        qt_q10 """
        select nation, city, count(*) from partition_table group by nation, city order by nation, city;
    """
    }

    def q11 = {
        qt_q11 """
        select city, count(*) from partition_table group by city order by city;
    """
    }

    def q12 = {
        qt_q12 """
        select nation, count(*) from partition_table group by nation order by nation;
    """
    }

    def q13 = {
        qt_q13 """
        select city, l_orderkey, nation from partition_table order by l_orderkey limit 10;
    """
    }

    def q14 = {
        qt_q14 """
        select l_orderkey from partition_table order by l_orderkey limit 10;
    """
    }

    def q15 = {
        qt_q15 """
        select count(l_orderkey) from partition_table;
    """
    }

    def q16 = {
        qt_q16 """
        select count(*) from partition_table where city='beijing';
    """
    }

    def q17 = {
        qt_q17 """
        select count(*) from partition_table where nation='cn';
    """
    }


    def set_be_config = { flag ->
        String[][] backends = sql """ show backends; """
        assertTrue(backends.size() > 0)
        for (String[] backend in backends) {
            StringBuilder setConfigCommand = new StringBuilder();
            setConfigCommand.append("curl -X POST http://")
            setConfigCommand.append(backend[2])
            setConfigCommand.append(":")
            setConfigCommand.append(backend[5])
            setConfigCommand.append("/api/update_config?")
            String command1 = setConfigCommand.toString() + "enable_new_load_scan_node=$flag"
            logger.info(command1)
            String command2 = setConfigCommand.toString() + "enable_new_file_scanner=$flag"
            logger.info(command2)
            def process1 = command1.execute()
            int code = process1.waitFor()
            assertEquals(code, 0)
            def process2 = command2.execute()
            code = process1.waitFor()
            assertEquals(code, 0)
        }
    }

    String enabled = context.config.otherConfigs.get("enableHiveTest")
    if (enabled != null && enabled.equalsIgnoreCase("true")) {
        try {
            String hms_port = context.config.otherConfigs.get("hms_port")
            sql """admin set frontend config ("enable_multi_catalog" = "true")"""
            sql """admin set frontend config ("enable_new_load_scan_node" = "true");"""
            set_be_config.call('true')
            sql """drop catalog if exists hive"""
            sql """
            create catalog if not exists hive properties (
                "type"="hms",
                'hive.metastore.uris' = 'thrift://127.0.0.1:${hms_port}'
            );
            """
            sql """use `hive`.`default`"""

            q01()
            q02()
            q03()
            q04()
            q05()
            q06()
            q07()
            q08()
            q09()
            q10()
            q11()
            q12()
            q13()
            q14()
            q15()
            q16()
            q17()
        } finally {
            sql """ADMIN SET FRONTEND CONFIG ("enable_new_load_scan_node" = "false");"""
            set_be_config.call('false')
        }
    }
}

