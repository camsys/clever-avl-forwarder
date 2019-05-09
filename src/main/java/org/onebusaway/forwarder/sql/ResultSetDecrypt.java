/**
 * Copyright 2012 University of South Florida
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package org.onebusaway.forwarder.sql;

import org.onebusaway.forwarder.models.CleverAvlData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * 
 * @author Khoa Tran
 *
 */

public class ResultSetDecrypt {
	private ResultSet rs;
	private ResultSetMetaData rsmd;
	private HashMap<String, Integer> columnInfo;
    private static final Logger _log = LoggerFactory.getLogger(ResultSetDecrypt.class);


    public ResultSetDecrypt(ResultSet rs){
		this.rs = rs;
		try {
			this.rsmd = rs.getMetaData();
			columnInfo = new HashMap<String, Integer>();
			columnInfo.putAll(getColumnInfo(rsmd));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public ArrayList<CleverAvlData> decrypt(){
		ArrayList<CleverAvlData> transitData = new ArrayList<CleverAvlData>();
		try {
			while(rs.next()){
				CleverAvlData td = convertRowToTransitData();
			    if(td!=null)
			        transitData.add(td);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return transitData;
	}

	private HashMap<String, Integer> getColumnInfo(ResultSetMetaData rsmd) throws SQLException{
		HashMap<String, Integer> columnInfo = new HashMap<String, Integer>();
		for (int i = 1; i <= rsmd.getColumnCount(); i++) {
			columnInfo.put(rsmd.getColumnName(i), rsmd.getColumnType(i));
		}
		return columnInfo;
	}

	public CleverAvlData convertRowToTransitData() {
        Iterator<String> columnNames = columnInfo.keySet().iterator();

        CleverAvlData avlData = null;

        while (columnNames.hasNext()) {
                String colName = columnNames.next();
                Object data = getDataWithCorrectDataType( colName, columnInfo.get(colName));

                if(data == null)
                    continue;

                if(avlData == null) {
                    avlData = new CleverAvlData();
                }

                avlData.setCorrectTransitDataProperties(colName, data);
        }
        return avlData;
    }

	private Object getDataWithCorrectDataType(String colName, int datatype) {
        try {
            switch (datatype) {
                case Types.CHAR:
                case Types.CLOB:
                case Types.VARCHAR:
                case Types.LONGNVARCHAR:
                case Types.NVARCHAR:
                case Types.NCHAR:
                    try {
                        return (String) rs.getString(colName);
                    } catch (NullPointerException npe) {
                        return "";
                    }
                case Types.BIT:
                case Types.BOOLEAN:
                    return (Boolean) rs.getBoolean(colName);
                case Types.INTEGER:
                case Types.SMALLINT:
                case Types.TINYINT:
                    return (Integer) rs.getInt(colName);
                case Types.BIGINT:
                    return (Long) rs.getLong(colName);
                case Types.DOUBLE:
                case Types.REAL:
                case Types.FLOAT:
                    return (Double) rs.getDouble(colName);
                case Types.DECIMAL:
                    return (BigDecimal) rs.getBigDecimal(colName);
                case Types.TIMESTAMP:
                    return (Timestamp) rs.getTimestamp(colName);
                case Types.DATE:
                    return (Date) rs.getDate(colName);
                case Types.TIME:
                    return (Time) rs.getTime(colName);
                case Types.BINARY:
                case Types.VARBINARY:
                    return (byte[]) rs.getBytes(datatype);
                default: {
                    System.out.println("ERROR: unknown sql datatype (" + datatype + ") for column: " + colName);
                    return null;
                }
            }
        } catch (SQLException ex) {
            System.out.println("SQL Exception ERROR at " + colName + ": " + ex.getMessage());
        }
        return null;
    }
}
