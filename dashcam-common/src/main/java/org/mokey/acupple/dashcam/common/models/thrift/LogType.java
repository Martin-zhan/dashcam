/**
 * Autogenerated by Thrift Compiler (0.9.2)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package org.mokey.acupple.dashcam.common.models.thrift;


import org.apache.thrift.TEnum;

public enum LogType implements TEnum {
  OTHER(0),
  APP(1),
  URL(2),
  WEB_SERVICE(3),
  SQL(4),
  MEMCACHED(5);

  private final int value;

  private LogType(int value) {
    this.value = value;
  }

  /**
   * Get the integer value of this enum value, as defined in the Thrift IDL.
   */
  public int getValue() {
    return value;
  }

  /**
   * Find a the enum type by its integer value, as defined in the Thrift IDL.
   * @return null if the value is not found.
   */
  public static LogType findByValue(int value) {
    switch (value) {
      case 0:
        return OTHER;
      case 1:
        return APP;
      case 2:
        return URL;
      case 3:
        return WEB_SERVICE;
      case 4:
        return SQL;
      case 5:
        return MEMCACHED;
      default:
        return null;
    }
  }
}
