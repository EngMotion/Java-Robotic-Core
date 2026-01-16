package com.lucaf.robotic_core.wenglor.p3pcxxx;

public class Indexes {
    // Identification
    public static final int VENDOR_NAME = 0x0010; 
    public static final int VENDOR_TEXT = 0x0011; 
    public static final int PRODUCT_NAME = 0x0012; 
    public static final int PRODUCT_ID = 0x0013; 
    public static final int PRODUCT_TEXT = 0x0014; 
    public static final int SERIAL_NUMBER = 0x0015; 
    public static final int HARDWARE_VERSION = 0x0016; 
    public static final int FIRMWARE_VERSION = 0x0017;
    // Tags
    public static final int APPLICATION_SPECIFIC_TAG = 0x0018; 
    public static final int FUNCTION_TAG = 0x0019; 
    public static final int LOCATION_TAG = 0x001A;
    // Sensor localization
    public static final int FIND_ME = 0x1200;
    // Device Settings
    public static final int SYSTEM_COMMAND = 0x0002; 
    public static final int DEVICE_ACCESS_LOCKS = 0x000C; 
    public static final int LANGUAGE = 0x00F0;
    public static final int DISPLAY_ROTATE = 0x00A0;
    // Measurement Settings
    public static final int SENSITIVITY = 0x0115; 
    public static final int CAPTURE_MODE = 0x0202; 
    public static final int MAX_EXPOSURE_TIME = 0x07D3; 
    public static final int MEASUREMENT_FILTER = 0x0110; 
    public static final int EMITTED_LIGHT = 0x00E0; 
    public static final int DETECTION_RANGE_NEAR = 0x0112; 
    public static final int DETECTION_RANGE_FAR = 0x0113; 
    public static final int OFFSET = 0x0116;
    public static final int OFFSET_PRESET = 0x04FE;
    public static final int APPLY_OFFSET_PRESET = 0x0500;
    public static final int LASER_CLASS = 0x00E2;
    public static final int LICENSE_ACTIVE = 0x00E4;
    public static final int LASER_CLASS_LICENSE_KEY = 0x00E3;
    // Switching Signal Channel 1 (SSC1)
    public static final int SSC1_TEACH_MODE = 0x0290; 
    public static final int SSC1_SWITCH_POINT = 0x0270; 
    public static final int SSC1_HYSTERESIS_MODE = 0x0230; 
    public static final int SSC1_HYSTERESIS_VALUE = 0x0300; 
    public static final int SSC1_WINDOW_NEAR = 0x0271; 
    public static final int SSC1_WINDOW_FAR = 0x0272;
    // Switching Signal Channel 2 (SSC2)
    public static final int SSC2_TEACH_MODE = 0x0291; 
    public static final int SSC2_SWITCH_POINT = 0x0280; 
    public static final int SSC2_HYSTERESIS_MODE = 0x0231; 
    public static final int SSC2_HYSTERESIS_VALUE = 0x0301; 
    public static final int SSC2_WINDOW_NEAR = 0x0281; 
    public static final int SSC2_WINDOW_FAR = 0x0282;
    // Pin functions
    public static final int EA1_PIN_FUNCTION = 0x0040; 
    public static final int EA2_PIN_FUNCTION = 0x0041; 
    public static final int E3_PIN_FUNCTION = 0x0042;
    // Digital Outputs
    public static final int A1_ON_DELAY = 0x0050; 
    public static final int A1_OFF_DELAY = 0x0060; 
    public static final int A1_NO_NC = 0x0210; 
    public static final int A1_NPN_PNP_PP = 0x0220;
    public static final int A2_ON_DELAY = 0x0051; 
    public static final int A2_OFF_DELAY = 0x0061; 
    public static final int A2_NO_NC = 0x0211; 
    public static final int A2_NPN_PNP_PP = 0x0221;
    // Digital Inputs
    public static final int E1_INPUT_UB_ACTIVE_INACTIVE = 0x0260; 
    public static final int E2_INPUT_UB_ACTIVE_INACTIVE = 0x0261; 
    public static final int E3_INPUT_UB_ACTIVE_INACTIVE = 0x0262;
    // Analog Output
    public static final int O_ANALOG_TEACH_IN = 0x0080; 
    public static final int O_ANALOG_TEACH_MODE = 0x0085; 
    public static final int O_ANALOG_OUTPUT_MODE = 0x0083; 
    public static final int O_ANALOG_SUBSTITUTE_VALUES = 0x0084; 
    public static final int O_ANALOG_0V_4MA = 0x0081; 
    public static final int O_ANALOG_10V_20MA = 0x0082; 
    public static final int O_TOLERANCE_RANGE = 0x0087; 
    public static final int O_TOLERANCE_CHARACTERISTIC = 0x0088; 
    public static final int ANALOG_5V_12MA = 0x0086;
    // Difference and Thickness Measurement
    public static final int SENSOR_MODE = 0x0111;
    public static final int SENSOR_MODE_EFFECTIVE = 0x0117;
    public static final int REFERENCE = 0x04FF;
    public static final int SENSOR_ALIGNMENT_OFFSET = 0x0501;
    public static final int SENSOR_GAP_THICKNESS = 0x0502;
    public static final int REFERENCE_VALUE = 0x0503;
    public static final int SSC1_SWITCH_POINT_COUPLED = 0x0504;
    public static final int SSC2_SWITCH_POINT_COUPLED = 0x0505;
    public static final int COUPLED_TOLERANCE_RANGE = 0x0506;
    public static final int COUPLED_CHARACTERISTIC = 0x0507;
    // Bluetooth
    public static final int BLUETOOTH = 0x0306;
    public static final int PASSWORD_PROTECTION = 0x0100;
    public static final int PASSWORD_CHANGE = 0x0101;
    // Coupled Mode Valuels
    public static final int DISTANCE_MAIN = 0x1216; 
    public static final int DISTANCE_SECONDARY = 0x1215; 
    public static final int MEASUREMENT_VALUE_DIFF_THICK = 0x1217; 
    public static final int INTENSITY = 0x1220;
    // Diagnosis
    public static final int DEVICE_STATUS = 0x0024; 
    public static final int DETAILED_DEVICE_STATUS = 0x0025; 
    public static final int ADDITIONAL_STATUS_INFORMATION = 0x1300; 
    public static final int SELF_CHECK = 0x2518; 
    public static final int INDICATION_WARNING_ERROR_1 = 0x1310; 
    public static final int INDICATION_WARNING_ERROR_2 = 0x1311; 
    public static final int INDICATION_WARNING_ERROR_3 = 0x1312; 
    public static final int INDICATION_WARNING_ERROR_4 = 0x1313; 
    public static final int WARNING_OUTPUT_CONFIGURATION = 0x1314; 
    public static final int ERROR_OUTPUT_CONFIGURATION = 0x1315; 

    //MEASURING DATA CHANNEL
    public static final int MEASURING_DATA_CHANNEL = 0x4080; 

    // DEVICE SIMULATION
    public static final int SIMULATION_MODE = 0x0310; 
    public static final int SIMULATION_MEASUREMENT_VALUE = 0x0315; 
    public static final int SIMULATION_SECONDARY_DISTANCE = 0x0333; 
    public static final int SIMULATION_SSC1 = 0x0331; 
    public static final int SIMULATION_SSC2 = 0x0332; 
    public static final int SIMULATION_ANALOG_OUTPUT_CURRENT = 0x0316; 
    public static final int SIMULATION_SIGNAL_WARNING = 0x031B; 
    public static final int SIMULATION_OVEREXPOSED = 0x031C; 
    public static final int SIMULATION_AMBIENT_LIGHT = 0x031E; 
    public static final int SIMULATION_FATAL_ERROR = 0x0323; 
    public static final int SIMULATION_TEMPERATURE_ERROR = 0x0324; 
    public static final int SIMULATION_TEMP_WARNING_HIGH = 0x0325; 
    public static final int SIMULATION_TEMP_WARNING_LOW = 0x032F; 
    public static final int SIMULATION_UNDERVOLTAGE = 0x0327; 
    public static final int SIMULATION_SHORT_CIRCUIT = 0x0328; 
    public static final int SIMULATION_LASER_ERROR = 0x032D; 
}
