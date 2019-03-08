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

	/** type of prediction */
	public static final int SinglePixelPrediction = 0;
	public static final int FilterPrediction = 1;
}
