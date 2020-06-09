/* ###################################################################### */
/* # # */
/* # # */
/* # Copyright (c) 2001 ISKRATEL # */
/* # # */
/* # # */
/* # Name : CDRpropertyClass.java # */
/* # # */
/* # Decription : Defines keys under which cdr data is saved. # */
/* # # */
/* # Code : GQRW - XAE5498 # */
/* # # */
/* # Date : July, 2001 # */
/* # # */
/* # Author : Vidic Ales, RDSM3 # */
/* # # */
/* # Translation : # */
/* # # */
/* # Remarks : # */
/* # # */
/* ###################################################################### */

package si.iskratel.cdr.parser;

import java.util.*;

/**
 * The PropertyClass class contains all keys for Hashtable and implements methods for seting date and time and methods
 * for printing data to display. One method is used for computing millisecons into hours,minutes,seconds,milliseconds.
 * 
 * @see ResourceBundle
 * @since JDK 1.3
 * @author Vidic Ales
 * @version 1.0, April,2001
 */
public class CdrProperty {

  public static final String CDR_BYTES = "CDR_BYTES";

  public static final String IE122_CBNO = "IE122_CBNO";

  public static final String IE131_NDN = "IE131_NDN";

  public static final String CENTREX_CALL_TYPE_B = "CENTREX_CALL_TYPE_B";

  public static final String BUSINESS_GROUP_B = "BUSINESS_GROUP_B";

  public static final String CENTREX_GROUP_B = "CENTREX_GROUP_B";

  public static final String LAC = "LAC";

  public static final String IE139_ADDITIONAL_CGN = "IE139_ADDITIONAL_CGN";

  public static final String IE142_NUMBER = "IE142_NUMBER";

  public static final String LENGTH_OF_NUMBER = "LENGTH_OF_NUMBER";

  public static final String NUMBERING_PLAN = "NUMBERING_PLAN";

  public static final String NATURE_OF_ADDR = "NATURE_OF_ADDR";

  public static final String THIRD_PARTY_TYPE = "THIRD_PARTY_TYPE";

  public static final String CALL_TYPE = "CALL_TYPE";

  public static final String IE144_Incoming_TD = "IE144_Incoming_TD";
  public static final String IE144_Incoming_Trunk_Group_Name = "IE144_Incoming_Name";

  public static final String IE145_Outgoing_TD = "IE145_Outgoing_TD";
  public static final String IE145_Outgoing_Trunk_Group_Name = "IE145_Outgoing_Name";

  /**
   * Specified key name in Hashtable. This key define name for type of cdr record.
   */
  public static final String RECORD_TYPE = "RECORD_TYPE";

  /**
   * Specified key name in Hashtable. This key define name for sequence call number in fixed part of call record.
   */
  public static final String SEQUENCE_CALL_NUMBER = "SEQUENCE_CALL_NUMBER";

  /**
   * Specified key name in Hashtable. This key define name for call identifier in fixed part of call record.
   */
  public static final String CALL_ID = "CALL_ID";
  public static final String ORIG_CALL_ID = "ORIG_CALL_ID";
  public static final String NAS_ID = "NAS_ID";

  public static final String CDR_INDEX = "CDR_INDEX";

  /** Specified key name in Hashtable. Key defines is connection record. */
  public static final String FLAG_F1 = "F1";

  /**
   * Specified key name in Hashtable. Key defines is record of performing supplement service (FAU - Facility Usage).
   */
  public static final String FLAG_F2 = "F2";

  /**
   * Specified key name in Hashtable. Key defines is record of Facility Input by Subscriber (FAIS).
   */
  public static final String FLAG_F3 = "F3";

  /** Specified key name in Hashtable. Key defines is connection succeed. */
  public static final String FLAG_F4 = "F4";

  /**
   * Specified key name in Hashtable. Key defines is connection charging with counters.
   */
  public static final String FLAG_F5 = "F5";

  /**
   * Specified key name in Hashtable. Key defines is connection charging with AMA records.
   */
  public static final String FLAG_F6 = "F6";

  /**
   * Specified key name in Hashtable. Key defines is AMA with immediate extract.
   */
  public static final String FLAG_F7 = "F7";

  /** Specified key name in Hashtable. Key defines is DEB (Detailed Billing). */
  public static final String FLAG_F8 = "F8";

  /**
   * Specified key name in Hashtable. Key defines is DEB with immediate extract.
   */
  public static final String FLAG_F9 = "F9";

  /**
   * Specified key name in Hashtable. Key defines is OMOB (Originating calls Meter Observation, source calls).
   */
  public static final String FLAG_F10 = "F10";

  /**
   * Specified key name in Hashtable. Key defines is TMOB (Terminating calls Meter Observation, sink calls).
   */
  public static final String FLAG_F11 = "F11";

  /**
   * Specified key name in Hashtable. Key defines is PMOB (Preventive Meter Observation).
   */
  public static final String FLAG_F12 = "F12";

  /**
   * Specified key name in Hashtable. Key defines is PMOB with immediate copy out.
   */
  public static final String FLAG_F13 = "F13";

  /** Specified key name in Hashtable. Key define is reversed charging. */
  public static final String FLAG_F14 = "F14";

  /**
   * Specified key name in Hashtable. Key define is record of connection, interrupted for switch/cutout of system.
   */
  public static final String FLAG_F15 = "F15";

  /** Specified key name in Hashtable. Key define is sink meter charging. */
  public static final String FLAG_F16 = "F16";

  /** Specified key name in Hashtable. Key define is centrex call. */
  public static final String FLAG_F17 = "F17";

  /** Specified key name in Hashtable. Key define is prepaid call or service. */
  public static final String FLAG_F18 = "F18";

  /**
   * Specified key name in Hashtable. This key define name for record sequence in fixed part of call record.
   */
  public static final String RECORD_SEQUENCE = "RECORD_SEQUENCE";

  /**
   * Specified key name in Hashtable. This key define name for area code and directory number of the subscriber to whom
   * the record belongs
   */
  public static final String OWNER_NUMBER = "OWNER_NUMBER";

  /**
   * Specified key name in Hashtable for variable part in call record. This key name is associate with partner directory
   * number. IE100
   */
  public static final String CALLED_NUMBER = "CALLED_NUMBER";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with partner
   * directory number. IE140
   */
  public static final String CALLED_NUMBER_FORMATTED = "CALLED_NUMBER_FORMATTED";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with partner
   * redirecting number. IE143
   */
  public static final String REDIRECTING_NUMBER = "REDIRECTING_NUMBER";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with directory number
   * of the subscriber to whom the call has been transferred.
   */
  public static final String TRANSFERRED_TO_DN = "TRANSFERRED_TO_DN";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with subscriber's
   * report.
   */
  public static final String FLAG_101 = "F101";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name refer on connection length.
   */
  public static final String FLAG_102 = "F102";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with time
   * reliability.
   */
  public static final String FLAG_103 = "F103";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with number of meter
   * pulses.
   */
  public static final String CHARGE_UNITS = "CHARGE_UNITS";

  public static final String CHARGE_STATUS = "CHARGE_STATUS";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with bearer service.
   */
  public static final String BEARER_SERVICE = "BEARER_SERVICE";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with teleservice.
   */
  public static final String TELESERVICE = "TELESERVICE";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with supplementary
   * service of originating subscriber.
   */
  public static final String ORIG_SUPPLEMENTARY_SERVICE = "ORIG_SUPPLEMENTARY_SERVICE";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with supplementary
   * service of the terminating subscriber.
   */
  public static final String TERM_SUPPLEMENTARY_SERVICE = "TERM_SUPPLEMENTARY_SERVICE";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with type of input.
   */
  public static final String TYPE_OF_INPUT = "TYPE_OF_INPUT";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with supplementary
   * service for facility input by subscriber.
   */
  public static final String FACILITY_INPUT = "FACILITY_INPUT";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with character
   * sequence.
   */
  public static final String DIGIT_STRING = "DIGIT_STRING";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with origin category.
   */
  public static final String ORIGIN_CATEGORY = "ORIGIN_CATEGORY";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with tariff
   * direction.
   */
  public static final String TARIFF_DIRECTION = "TARIFF_DIRECTION";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with reason of
   * unsuccessfil call.
   */
  public static final String FAILURE_CAUSE = "FAILURE_CAUSE";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with reason of call
   * release.
   */
  public static final String CALL_RELEASE_CAUSE = "CALL_RELEASE_CAUSE";

  public static final String CALL_RELEASE_VALUE = "CALL_RELEASE_CAUSE_VALUE";

  public static final String CALL_RELEASE_STANDARD = "CALL_RELEASE_STANDARD";

  public static final String CALL_RELEASE_LOCATION = "CALL_RELEASE_LOCATION";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with identification
   * of the incoming trunk group.
   */
  public static final String INTRUNK_GROUP_ID = "INTRUNK_GROUP_ID";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with identification
   * of the incoming trunk.
   */
  public static final String INTRUNK_ID = "INTRUNK_ID";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with identification
   * of the incoming module.
   */
  public static final String INMODULE_ID = "INMODULE_ID";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with identification
   * of the incoming port.
   */
  public static final String INPORT_ID = "ID-INPORT_ID";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with identification
   * of the incoming channel.
   */
  public static final String INCHANNEL_ID = "INCHANNEL_ID";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with identification
   * of the outgoing trunk group.
   */
  public static final String OUTTRUNK_GROUP_ID = "OUTTRUNK_GROUP_ID";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with identification
   * of the outgoing trunk.
   */
  public static final String OUTTRUNK_ID = "OUTTRUNK_ID";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with identification
   * of the outgoing module.
   */
  public static final String OUTMODULE_ID = "OUTMODULE_ID";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with identification
   * of the outgoing port.
   */
  public static final String OUTPORT_ID = "OUTPORT_ID";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with identification
   * of the outgoing channel.
   */
  public static final String OUTCHANNEL_ID = "OUTCHANNEL_ID";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with duration of
   * call.
   */
  public static final String CALL_DURATION = "CALL_DURATION";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with checksum.
   */
  public static final String CHECKSUM = "CHECKSUM";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with centrex group
   * mark.
   */
  public static final String CENTREX_GROUP = "CENTREX_GROUP";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with business group
   * mark.
   */
  public static final String BUSINESS_GROUP = "BUSINESS_GROUP";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with CAC type at
   * network access code.
   */
  public static final String CAC_TYPE = "CAC_TYPE";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with CAC number at
   * network access code.
   */
  public static final String CAC_NUMBER = "CAC_NUMBER";
  public static final String CAC_PREFIX = "CAC_PREFIX";
  
  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with VoIP Info
   */
  public static final String VOIP_RX_CODEC_TYPE = "VOIP_RX_CODEC_TYPE";
  public static final String VOIP_TX_CODEC_TYPE = "VOIP_TX_CODEC_TYPE";
  public static final String VOIP_SIDE = "VOIP_SIDE";
  public static final String VOIP_CALL_TYPE = "VOIP_CALL_TYPE";
  
  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with Amount of transferred data
   */
  public static final String VOIP_RX_PACKETS = "VOIP_RX_PACKETS";
  public static final String VOIP_TX_PACKETS = "VOIP_TX_PACKETS";
  public static final String VOIP_RX_OCTETS = "VOIP_RX_OCTETS";
  public static final String VOIP_TX_OCTETS = "VOIP_TX_OCTETS";
  public static final String VOIP_PACKETS_LOST = "VOIP_PACKETS_LOST";
  public static final String VOIP_AVERAGE_JITTER = "VOIP_AVERAGE_JITTER";
  public static final String VOIP_AVERAGE_LATENCY = "VOIP_AVERAGE_LATENCY";
  
  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with VoIP Quality of Service Data
   */
  public static final String VOIP_ECHO_RETURN_LOSS = "VOIP_ECHO_RETURN_LOSS";
  public static final String VOIP_PACKETS_SENT_AND_LOST = "VOIP_PACKETS_SENT_AND_LOST";
  public static final String VOIP_MAX_PACKETS_LOST_IN_BURST = "VOIP_MAX_PACKETS_LOST_IN_BURST";
  public static final String VOIP_MAX_JITTER = "VOIP_MAX_JITTER";
  public static final String VOIP_MIN_JITTER = "VOIP_MIN_JITTER";
  public static final String VOIP_RX_MOS = "VOIP_RX_MOS";
  public static final String VOIP_TX_MOS = "VOIP_TX_MOS";
  public static final String VOIP_FAX_MODULATION_TYPE = "VOIP_FAX_MODULATION_TYPE";
  public static final String VOIP_FAX_TRANSFER_RATE = "VOIP_FAX_TRANSFER_RATE";
  public static final String VOIP_FAX_MODEM_RETRAINS = "VOIP_FAX_MODEM_RETRAINS";
  public static final String VOIP_FAX_PAGES_TRANSFERRED = "VOIP_FAX_PAGES_TRANSFERRED";
  public static final String VOIP_FAX_PAGES_REPEATED = "VOIP_FAX_PAGES_REPEATED";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with original calling
   * subscriber's number.
   */
  public static final String ORIGINAL_CPN = "ORIGINAL_CPN";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with request type at
   * information about filling prepaid account.
   */
  public static final String REQUEST_TYPE = "REQUEST_TYPE";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with new tariff inits
   * number at information about filling prepaid account.
   */
  public static final String ADDED_CHARGE_UNITS = "ADDED_CHARGE_UNITS";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with new state on
   * prepaid account at information about filling prepaid account.
   */
  public static final String NEW_BALANCE = "NEW_BALANCE";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with new termination
   * date of prepaid account at information about filling prepaid account.
   */
  public static final String NEW_EXPIRED_DATE = "NEW_EXPIRED_DATE";

  /** Specified key name in Hashtable for previous time and date. */
  public static final String PREVIOUS_DATE_AND_TIME = "PREVIOUS_DATE_AND_TIME";

  /** Specified key name in Hashtable for new time and date. */
  public static final String NEW_DATE_AND_TIME = "NEW_DATE_AND_TIME";

  /** Specified key name in Hashtable of reason for change. */
  public static final String CHANGE_CAUSE = "CHANGE_CAUSE";

  /**
   * Specified key name in Hashtable. This key define name for start date and time of loosing records.
   */
  public static final String LOST_RECORDS_START = "LOST_RECORDS_START";

  /**
   * Specified key name in Hashtable. This key define name for end date and time of loosing records.
   */
  public static final String LOST_RECORDS_END = "LOST_RECORDS_END";

  /**
   * Specified key name in Hashtable. This key define name for number of lost records.
   */
  public static final String NUMBER_OF_LOST_RECORDS = "NUMBER_OF_LOST_RECORDS";

  /**
   * Specified key name in Hashtable. This key define name for date and time of exchange restart.
   */
  public static final String RESTART_TIME = "RESTART_TIME";

  /** Specified key name in Hashtable. This key define name for call records. */
  public static final String RECORD_200 = "RECORD_200";

  /**
   * Specified key name in Hashtable. This key define name for record about date or time change.
   */
  public static final String RECORD_210 = "RECORD_210";

  /**
   * Specified key name in Hashtable. This key define name for records about lost records.
   */
  public static final String RECORD_211 = "RECORD_211";

  /**
   * Specified key name in Hashtable. This key define name for record about central restart.
   */
  public static final String RECORD_212 = "RECORD_212";

  /**
   * Specified key name in Hashtable. This key is designation for fixed part of call record.
   */
  public static final String FIXED = "FIXED";

  /**
   * Specified key name in Hashtable. This key is designation for variabled part of call record.
   */
  public static final String VARIABLE = "VARIABLE";

  /**
   * Specified key name in Hashtable. This key is designation for flags in fixed part of call record.
   */
  public static final String FLAGS = "FLAGS";

  /**
   * Specified key name in Hashtable. This key is designation for XML Start-tag.
   */
  public static final String ALL_RECORDS = "ALL_RECORDS";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with date of call
   * start.
   */
  public static final String CALL_START_TIME = "CALL_START_TIME";

  /**
   * Specified key name in Hashtable for variabled part in call record. This key name is associate with date of call
   * end.
   */
  public static final String CALL_STOP_TIME = "CALL_STOP_TIME";

  /**
   * Flags for IE102
   */
  public static final String ORIG_TIME_ZONE = "ORIG_TIME_ZONE";
  public static final String TERM_TIME_ZONE = "TERM_TIME_ZONE";
  public static final String UTC_TIME_ZONE = "UTC_TIME_ZONE";// Time is in UTC time zone

  public static final String PRICE = "PRICE";

  /**
   * IE137
   */
  public static final String SUPPLEMENTARY_SERVICE_INFO = "SUPPLEMENTARY_SERVICE_INFO";

  public static final String SUPPLEMENTARY_SERVICE_INFO_CALLED = "SUPPLEMENTARY_SERVICE_INFO_CALLED";
  public static final String SUPPLEMENTARY_SERVICE_INFO_SCI = "SUPPLEMENTARY_SERVICE_INFO_SCI";

  /**
   * IE147
   */
  public static final String GLOBAL_CALL_REFERENCE = "GLOBAL_CALL_REFERENCE";

  /**
   * IE124
   */
  public static final String TIME_BEFORE_RINGING = "TIME_BEFORE_RINGING";

  public static final String RINGING_TIME_BEFORE_ANSWER = "RINGING_TIME_BEFORE_ANSWER";

  /**
   * IE151
   */
  public static final String IE151_CALL_TYPE = "IE151_CALL_TYPE";

  /**
   * IE159
   */
  public static final String CTX_CALLING_NUMBER = "CTX_CALLING_NUMBER";
  public static final String CTX_CALLED_NUMBER = "CTX_CALLED_NUMBER";
  public static final String CTX_REDIRECTING_NUMBER = "CTX_REDIRECTING_NUMBER";

  /**
   * IE156 (additional numbers)
   */
  public static final String CONNECTED_NUMBER = "CONNECTED_NUMBER";

  /**
   * IE135 (IMS charging identifier - ICID)
   */
  public static final String ICID = "ICID";
  public static final String ICID_HOSTNAME = "ICID_HOSTNAME";
  
  /**
   * IE134 (Additional Statistics Data)
   */
  public static final String CALLING_SUBSCRIBER_GROUP = "CALLING_SUBSCRIBER_GROUP";
  public static final String CALLED_SUBSCRIBER_GROUP = "CALLED_SUBSCRIBER_GROUP";
  public static final String ORIG_SIDE_SND_LINE_TYPE = "ORIG_SIDE_SND_LINE_TYPE";
  public static final String TERM_SIDE_SND_LINE_TYPE = "TERM_SIDE_SND_LINE_TYPE";
  public static final String CALL_RELEASING_SIDE = "CALL_RELEASING_SIDE";

  /**
   * IE160 (Time Zone)
   */
  public static final String TIME_ZONE_REFERENCE = "TIME_ZONE_REFERENCE";
  public static final String TIME_ZONE_OFFSET = "TIME_ZONE_OFFSET";
  public static final String TIME_ZONE_NAME = "TIME_ZONE_NAME";

}
