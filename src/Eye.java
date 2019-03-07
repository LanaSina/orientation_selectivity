import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * each sensor is just a list of values that are activated or not.
 * Sensor should be an interface.
 * 
 * @author lana
 *
 */
public class Eye  {
	MyLog mlog = new MyLog("Eye", false);

	boolean seesWhite;
	VisionConfiguration configuration;
	
	/** total number of neurons sensitive to one grayscale value*/
	int neuronsByGrayscale;
	/** size of current image we're looking at*/
	int h,w;
	/** resolution of focused area = px/side of square */ 
	int e_res;	
	/** sensitivity resolution; number of distinct groups of sensory neurons*/
	int gray_scales;
	
	/** matrix of exact black and white values for the image [row][col] = white 0..1 black */
	double[][] bw;
	/** maps actual values in world to sensors (square sensory field, can be overlapping) 
	 * [sensor id][topmost sensor, leftmost sensor, size]*/
	int[][] eye_interface;
	//image through sensor
	int[] coarse;


	/** actual image from data folder*/
	BufferedImage imageInput;
	/** limited area fitting eye size*/
	BufferedImage coarseSpatialInput;
	/** actual eye input with coarse outfocus zone*/
	BufferedImage coarseColorInput;
	BufferedImage previous_eye_input_coarse;

	/** */
	//BufferedImage prediction;
	private BufferedImage prediction_t0;

	/**
	 *
	 */
	public Eye(VisionConfiguration configuration){
		this.configuration = configuration;

		//init
		/** size of focused area */
		e_res = configuration.e_res;
		gray_scales = configuration.gray_scales;
		h = configuration.h;
		w = configuration.w;
		seesWhite = configuration.seesWhite;
		
    	//number of neurons in visual field
    	neuronsByGrayscale = h*w/(e_res*e_res);
    	mlog.say("neurons in visual field "+ neuronsByGrayscale);
		
		//sensory field mapping to real world
		eye_interface = new int[neuronsByGrayscale][3];
		coarseColorInput = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		prediction_t0 = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		previous_eye_input_coarse = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		//spatial coarse graining
		coarse = new int[neuronsByGrayscale];

		//build interface
		//width
		int w = 0;
		//height
		int h = 0;
		int nn = 0;
		//left to right, top to down
		while(h<this.h){
			eye_interface[nn][0] = h;//row
			eye_interface[nn][1] = w;//col
			eye_interface[nn][2] = e_res;//size
			w+=e_res;//next column
			if(w >= this.w){//
				//next row
				h+=e_res;
				w=0;
			}
			nn++;
		}

		mlog.say("eye interface sites "+ nn);
	}


	/**
	 * reads an image 
	 * builds the black and white buffer bw
	 * also initializes im_h, im_w and bw
	 * @param name name of the image file (without path)
	 */
	public void readImage(String name){
		try {
			mlog.say("reading " + name);

			imageInput = ImageIO.read(new File(name));
			
			//black and white buffer for image
			//[row][column] = blackness level
			//bw = new double[im_h][im_w];
			bw = new double[w][h];

			//build the whole image
			for(int i=0; i<w;i++){
				for(int j=0; j<h;j++){
					Color color = new Color(imageInput.getRGB(i,j));
			        int b = color.getBlue();//0:255
			        int g = color.getGreen();
			        int r = color.getRed();
			        //convert to bw
			        double mean = (b+g+r)/(255*3.0);
			        //high value is black
			        double b2 = (1-mean);
			        bw[i][j] = b2;
				}				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	private BufferedImage copyImage(BufferedImage source){
		BufferedImage image =  new BufferedImage(source.getWidth(), source.getHeight(),
				source.getType());
		Graphics g = image.getGraphics();
		g.drawImage(source, 0, 0, null);
		g.dispose();

		return image;
	}


	/**
	 * builds "image" of sensory input, a coarse version of the original image
	 * from the motion of eye muscles. We use relative motion bc it takes less memory space (easier)
	 * @return always true...
	 */
	public boolean preProcessInput(){
		mlog.say("buildCoarse");

		coarseSpatialInput = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		previous_eye_input_coarse = copyImage(coarseColorInput);
		//previous_eye_input_coarse0 = copyImage(coarseColorInput);
		coarseColorInput = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

		//spatial coarse graining
		coarse = new int[neuronsByGrayscale];

		double[] sums = new double[neuronsByGrayscale];
		for(int i=0; i<neuronsByGrayscale; i++){
			sums[i] = 0;
		}	
		//calculate average color of each sensory field
		for(int k=0; k<neuronsByGrayscale; k++){//cool stuff can work with overlap too
			int sensor_j = eye_interface[k][0];//row
			int sensor_i = eye_interface[k][1];//col
			int size = eye_interface[k][2];//size of the zone for this sensor

			if(sensor_i>=0 & sensor_i+size<=w & sensor_j>=0 & sensor_j+size<=h){//?w h
				for(int i=sensor_i; i<sensor_i+size; i++){//row, x
					for(int j=sensor_j; j<sensor_j+size; j++){//col, y
						sums[k]+=bw[i][j];//[row][column] 
					}
				}		
			}
			//average
			sums[k] = sums[k]/(size*size);

			//convert to grayscales (color coarse graining)
			double grayscaleValue = sums[k]*(gray_scales-1);//gray_scales = 0...gray_scales-1
			coarse[k] = (int)(grayscaleValue+0.5);// + 1; //+1

			//build visualisation for UI
			//coarse space
			int b = (int)(255*(1-sums[k])+0.5);
			Color color = new Color(b,b,b);
			//coarse color
			double val = (coarse[k]*1.0/gray_scales)*255;
			b = (int)(255-val+0.5);
			Color color2 = new Color(b,b,b);

			int rel_j = eye_interface[k][0];//row, y
			int rel_i = eye_interface[k][1];//col, x
			for(int i=rel_i; i<rel_i+size; i++){//y
				for(int j=rel_j; j<rel_j+size; j++){//x	
					coarseSpatialInput.setRGB(i, j, color.getRGB());//j and i
					coarseColorInput.setRGB(i, j, color2.getRGB());
				}
			}
			
		}
		return true;
	}

	public int[][] getEyeInterface() {
		return eye_interface;
	}
	
	/** total number of neurons sensitive to one grayscale value*/
	public int getNeuronsByGrayscale() {
		return neuronsByGrayscale;
	}

	/**
	 * square filled with current grayscale value
	 * @return
	 */
	public int[] getCoarse(){
		return coarse;
	}
}
