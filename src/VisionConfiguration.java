import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class VisionConfiguration extends Configuration {
	public static final int KITTI = 0;
	public static final int OSWALD_20FPS = 1;


	//TODO this should be in starter?
	public static final int config = OSWALD_20FPS;//OSWALD_20FPS;//OSWALD_SMALL_20FPS;


	/**images files*/
	public String configuration_name;
	/**images files*/
	public String images_path; 
	/** leading zeros*/
	public  String name_format;
	/** number of images*/
	public  int n_images;
	public  String image_format;
	/** starting from*/
	public int start_number;

	/** sensitivity of the image sensor */
	public  int gray_scales ;
	/** resolution of focused area of eye*/
	public  int e_res;
	/** image dimensions*/
	public  int w;
	public   int h;
	public boolean seesWhite = true;

	public VisionConfiguration() {
		mlog.setName("VisionConfiguration");
		configuration_name = "UNDEFINED";

		gray_scales = 10;//15
		e_res = 1;

		int c = config;
		switch (c) {

		case OSWALD_20FPS:{
			configuration_name = "OSWALD_20FPS";

			images_path = Constants.OswaldFramesPath;
			name_format = "%04d";
			n_images = 6644;//
			image_format = ".bmp";
			start_number = 1;
			w = 480;
			h = 360;
			break;
		}

		case KITTI:{
			configuration_name = "KITTI";

			images_path = Constants.KittiFramesPath;
			name_format = "%03d";
			n_images = 453;
			image_format = ".png";
			start_number = 0;
			w = 418;
			h = 154;
			break;
		}

		default:
			break;
		}
		setupStrings();
	}

	public String getConfigurationName(){
		return configuration_name;
	}

	@Override
	protected void fillParameterMap(){
		parameterMap.put("images_path", "" + images_path);
		parameterMap.put("name_format", "" + name_format);
		parameterMap.put("n_images", "" + n_images);
		parameterMap.put("image_format", "" + image_format);
		parameterMap.put("start_number", "" + start_number);
		parameterMap.put("gray_scales", "" + gray_scales);
		parameterMap.put("e_res", "" + e_res);
		parameterMap.put("seesWhite", "" + seesWhite);

		parametersHeader = "";
		parametersValuesString = "";
		for (Iterator<Map.Entry<String, String>> parameterIterator = parameterMap.entrySet().iterator(); parameterIterator.hasNext();){
			Map.Entry pair = parameterIterator.next();
			parametersHeader += pair.getKey();
			parametersValuesString += pair.getValue();
			if(parameterIterator.hasNext()){
				parametersHeader+=",";
				parametersValuesString += ",";
			}
		}

	}
	
	public VisionConfiguration(HashMap<String, String> map) {
		this.parameterMap = map;

		images_path = map.get("images_path");
		name_format = map.get("name_format");
		n_images = Integer.parseInt(map.get("n_images"));
		image_format = map.get("image_format");
		start_number = Integer.parseInt(map.get("start_number"));
		gray_scales = Integer.parseInt(map.get("gray_scales"));
		e_res = Integer.parseInt(map.get("e_res"));
		seesWhite = Boolean.parseBoolean(map.get("seesWhite"));;
	}

	@Override
	public String toString() {
		String desc = getHeaders() + getValuesString();
		return desc;
	}

	public String getHeaders(){
		return parametersHeader;
	}

	public String getValuesString(){
		return  parametersValuesString;
	}
}
