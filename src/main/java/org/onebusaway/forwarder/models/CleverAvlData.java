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

package org.onebusaway.forwarder.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.onebusaway.forwarder.dao.CleverAvlDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;

/**
 * 
 * @author Khoa Tran
 *
 */

public class CleverAvlData {
	private String vehicleId, blockId, driverId;
	private double vehicleLat, vehicleLon;
	private Timestamp vehicleTime;
	private int vehicleSpeed, delay, vehicleBearing, sequenceNumber;
	private boolean fix;

  private static final Logger _log = LoggerFactory.getLogger(CleverAvlData.class);


  public void setCorrectTransitDataProperties(String colName, Object data){
        if(colName.equals("time")){
          this.setVehicleTime((Timestamp)data);
        } else if (colName.equals("vehicle_id")) {
          String vehicleId = padId(data.toString().trim());
          this.setVehicleId(vehicleId);
        } else if(colName.equals("latitude")){
          this.setVehicleLat((Double)data);
        } else if(colName.equals("longitude")){
          this.setVehicleLon((Double)data);
        } else if(colName.equals("heading")){
          this.setVehicleBearing((Integer)data);
        } else if(colName.equals("speed")){
          this.setVehicleSpeed((Integer)data);
        } else if(colName.equals("fix")){
          String fix = (String)data;
          if(fix != null && fix.equalsIgnoreCase("T")){
            this.setFix(Boolean.TRUE);
          } else {
            this.setFix(Boolean.FALSE);
          }
        } else if(colName.equals("gtfs_block_id")){
          this.setBlockId(data.toString());
        } else if(colName.equals("driver_id")){
            this.setDriverId(data.toString());
        } else {
            _log.trace("Cannot map "+colName+" = "+data.toString()+" to any type!");
        }
    }

    private String padId(String vehicleId){
      int idLength = vehicleId.length();
      
      if(idLength == 3){
        return "0" + vehicleId;
      } else if(idLength == 2){
        return "00" + vehicleId;
      } else if(idLength == 1){
        return "000" + vehicleId;
      }
      return vehicleId;
    }
	
	public String toString() {

      return "{ " +
                "timestamp: " + vehicleTime + ", " +
                "vehicle_id: " + vehicleId + ", " +
                "latitude: " + vehicleLat + ", " +
                "longitude: " + vehicleLon + ", " +
                "block_id: " + blockId + ", " +
                "driver_id: " + driverId + ", " +
                "speed: " + vehicleSpeed + ", " +
                "bearing: " + vehicleBearing + ", " +
                "fix: " + fix  +
              " }";
	}

  /**
   * @return the vehicleId
   */
  @JsonProperty("vehicleid")
  public String getVehicleId() {
    return vehicleId;
  }

  /**
   * @param vehicleId the vehicleId to set
   */
  public void setVehicleId(String vehicleId) {
    this.vehicleId = vehicleId;
  }


  /**
   * @return the vehicleLat
   */
  @JsonProperty("latitude")
  public double getVehicleLat() {
    return vehicleLat;
  }

  /**
   * @param vehicleLat the vehicleLat to set
   */
  public void setVehicleLat(double vehicleLat) {
    this.vehicleLat = vehicleLat;
  }

  /**
   * @return the vehicleLon
   */
  @JsonProperty("longitude")
  public double getVehicleLon() {
    return vehicleLon;
  }

  /**
   * @param vehicleLon the vehicleLon to set
   */
  public void setVehicleLon(double vehicleLon) {
    this.vehicleLon = vehicleLon;
  }

  /**
   * @return the vehicleSpeed
   */
  @JsonProperty("averageSpeed")
  public int getVehicleSpeed() {
    return vehicleSpeed;
  }

  /**
   * @param vehicleSpeed the vehicleSpeed to set
   */
  public void setVehicleSpeed(int vehicleSpeed) {
    this.vehicleSpeed = vehicleSpeed;
  }

  /**
   * @return the time
   */
  @JsonProperty("avlDate")
  public Timestamp getVehicleTime() {
    return vehicleTime;
  }

  /**
   * @param time the time to set
   */
  public void setVehicleTime(Timestamp vehicleTime) {
    this.vehicleTime = vehicleTime;
  }

  /**
   * @return the bearing
   */
  @JsonProperty("heading")
  public int getVehicleBearing() {
    return vehicleBearing;
  }

  /**
   * @param bearing the bearing to set
   */
  public void setVehicleBearing(int bearing) {
    this.vehicleBearing = bearing;
  }

  @JsonProperty("blockAlpha")
  public String getBlockId() {
    return blockId;
  }

  public void setBlockId(String blockId) {
    this.blockId = blockId;
  }

  @JsonProperty("driverId")
  public String getDriverId() {
    return driverId;
  }

  public void setDriverId(String driverId) {
    this.driverId = driverId;
  }

  @JsonProperty("delay")
  public int getDelay() {
    return delay;
  }

  public void setDelay(int delay) {
    this.delay = delay;
  }

  @JsonProperty("sequenceNumber")
  public int getSequenceNumber() {
    return sequenceNumber;
  }

  public void setSequenceNumber(int sequenceNumber) {
    this.sequenceNumber = sequenceNumber;
  }

  @JsonProperty("fix")
  public boolean isFix() {
    return fix;
  }

  public void setFix(boolean fix) {
    this.fix = fix;
  }
}
