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
//	/** size of visual field
//	 * ???? actually looks mofre like the number of modules.*//*
//	public  int vf_w;
//	public  int vf_h;*/
	/** image dimensions*/
	public  int w;
	public   int h;
//	/** image dimensions (viewed from eye) */
//	public  int iw;
//	public  int ih;
//	/**number of timesteps we train on each image*/
//	public int max_presentations;
//	/** for sensory layer */
//	public int square_module_size;
//
//	public boolean has_noise = false;
//	public int folder_number = 6;
	public boolean seesWhite = true;

	/** max number of neurons*/
	public int initial_neurons_per_module;


	public VisionConfiguration() {
		mlog.setName("VisionConfiguration");
		configuration_name = "UNDEFINED";

		//square_module_size = 30;
		//max_presentations = 1;
		gray_scales = 10;//15
		
		int c = config;
		switch (c) {

		case OSWALD_20FPS:{
			configuration_name = "OSWALD_20FPS";

			images_path = Constants.OswaldFramesPath;
			name_format = "%04d";
			n_images = 6644;//
			image_format = ".bmp";
			start_number = 1;
			//square_module_size = 480;

			w = 480;
			h = 360;
			e_res = 1;
			break;
		}

		case KITTI:{
			configuration_name = "KITTI";

			images_path = Constants.KittiFramesPath;
			name_format = "%010d";
			n_images = 2;//83;
			image_format = ".png";
			start_number = 0;

			e_res = 1;//2;
			w = 51;//79;
			h = 79;//51;
			break;
		}

		default:
			break;
		}
		
		
//		initial_neurons_per_module = square_module_size*square_module_size*gray_scales;//10*10 square
//
//		vf_w = (w/e_res)/square_module_size;//2.5->2.0
//
//		if( (w/e_res)%square_module_size >0){
//			vf_w+=1;//3
//		}
//
//		vf_w = vf_w*square_module_size*e_res;//60
//		mlog.say("vf_w " + vf_w);
//
//		vf_h = (h/e_res)/square_module_size;
//		if( (h/e_res)%square_module_size >0){
//			vf_h+=1;
//		}
//		vf_h = vf_h*square_module_size*e_res;
//		mlog.say("vf_h " + vf_h);

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
//		parameterMap.put("vf_w", "" + vf_w);
//		parameterMap.put("vf_h", "" + vf_h);
//		parameterMap.put("w", "" + w);
//		parameterMap.put("h", "" + h);
//		parameterMap.put("iw", "" + iw);
//		parameterMap.put("ih", "" + ih);
//		parameterMap.put("max_presentations", "" + max_presentations);
//		parameterMap.put("square_module_size", "" + square_module_size);
//		parameterMap.put("initial_neurons_per_module", "" + initial_neurons_per_module);
//		parameterMap.put("has_noise", "" + has_noise);
//		parameterMap.put("folder_number", "" + folder_number);
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
//		vf_w = Integer.parseInt(map.get("vf_w"));
//		vf_h = Integer.parseInt(map.get("vf_h"));
//		w = Integer.parseInt(map.get("w"));
//		h = Integer.parseInt(map.get("h"));
//		iw = Integer.parseInt(map.get("iw"));
//		ih = Integer.parseInt(map.get("ih"));
//		max_presentations = Integer.parseInt(map.get("max_presentations"));
//		square_module_size = Integer.parseInt(map.get("square_module_size"));
//		initial_neurons_per_module = Integer.parseInt(map.get("initial_neurons_per_module"));
//		has_noise = Boolean.parseBoolean(map.get("has_noise"));
//		folder_number = Integer.parseInt(map.get("folder_number"));
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
