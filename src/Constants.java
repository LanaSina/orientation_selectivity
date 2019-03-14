/**
 * Global variables
 * @author lana
 *
 */
public class Constants {
	private static boolean shouldLog = true;
	
	/** controls output of all loggers. Set true for verbose mode, false for dry mode.*/
	public static final boolean getShouldLog(){
		return shouldLog;
	}

	public static final boolean setShouldLog(boolean b){
		shouldLog = b;
		return b;
	}

	/** where data files will be created*/
	public static final String DataPath = "/Users/lana/Development/SNET_data/";

	public static final String ConfigurationFileName = "configuration.csv";
	public static final String FilterConfigurationFileName = "filter_configuration.csv";
	public static final String PredictionWeightsFileName = "prediction_weights.csv";
	public static final String OswaldFramesPath = "/Users/lana/Desktop/prgm/SNet/images/Oswald/full_20_fps/";
	public static final String KittiFramesPath = "/Users/lana/Desktop/prgm/SNet/images/KITTI/";
    public static final String CroppedKittiFramesPath = "/Users/lana/Desktop/prgm/SNet/images/cropped_KITTI/";
	public static final String CroppedOswaldFramesPath = "/Users/lana/Desktop/prgm/SNet/images/Oswald/cropped_Oswald/";
	public static final String DebugFramesPath = "/Users/lana/Desktop/prgm/SNet/images/Dataset_01/";
	public static final String FpsiFramesPath = "/Users/lana/Desktop/prgm/SNet/images/FPSI/";
	public static final String CroppedFpsiFramesPath = "/Users/lana/Desktop/prgm/SNet/images/cropped_FPSI/";
	public static final String KitchenFramesPath = "/Users/lana/Desktop/prgm/SNet/images/Kitchen/";
	public static final String CroppedKitchenFramesPath = "/Users/lana/Desktop/prgm/SNet/images/cropped_Kitchen/";

	/** types of prediction */
	public static final int SinglePixelPrediction = 0;
	public static final int FilterPrediction = 1;
	public static final int FilterToFilterPrediction = 2;
	public static final int PixelVelocityPrediction = 3;
	public static final int FilterVelocityPrediction = 4;
	public static final int PixelWholeVelocityPrediction = 5;
	public static final int FilterWholeVelocityPrediction = 6;
	public static final int DiscontinuityPrediction = 7;

	/** types of inputs */
	public static final int GreyscaleInput = 0;
	public static final int ContrastInput = 1;
}
