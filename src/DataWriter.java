import java.io.*;

public class DataWriter {
	/** log */
	MyLog mlog = new MyLog("DataWriter", true);
	
	private String folderName = null;
	private Configuration configuration;
	
	public DataWriter(String folderName, Configuration configuration) {
		mlog.say("DataWriter " + folderName);
		this.folderName = folderName;
		this.configuration = configuration;

		File theDir = new File(folderName);
		// if the directory does not exist, create it
		if (!theDir.exists()) {
			mlog.say("creating directory: " + folderName);
			boolean result = false;
			try{
				theDir.mkdirs();
				result = true;
			}
			catch(SecurityException se){
			    result = false;
			}
			if(result) {
				System.out.println("DIR created");
			}
		}

	}
	
	public void writeConfiguration(String subFolderName){
		mlog.say("writeConfiguration");
		String fileName = subFolderName+"/" + Constants.ConfigurationFileName;
		FileWriter writer;
		try {
			writer = new FileWriter(fileName);
			//write configuration
			writer.write(configuration.getHeaders()+"\n");
            writer.write(configuration.getValuesString());
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	public void writeErrors(int[] greyscales, int[] velocities, double[] errors, double[] defaultErrors) {
		mlog.say("writeErrors");
		String file = folderName + "/"+ Constants.PredictionWeightsFileName;
		mlog.say("writing in " + file);
		FileWriter writer;

		File theDir = new File(folderName);
		// if the directory does not exist, create it
		if (!theDir.exists()) {
			mlog.say("creating directory: " + folderName);
			boolean result = false;
			try{
				theDir.mkdirs();
				result = true;
			}
			catch(SecurityException se){
			}
			if(result) {
				System.out.println("DIR created");
			}
		}

		int written = 0;
		try {
			writer = new FileWriter(file);
			String str;

			//header 1
			str = "step, greyscale, estimated_v, error, default_error\n";
			writer.write(str);
			writer.flush();

			for (int step=0; step<velocities.length; step++) {
				str = step + "," + greyscales[step] + "," + velocities[step] + ","
						+ errors[step] + "," + defaultErrors[step] + "\n";
				writer.write(str);
				writer.flush();
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		mlog.say("wrote in " + file + " rows " + written);
	}

	public void writeSimplePrediction(int value, int age, int x, int y){
		mlog.say("writeSimplePrediction");
		String file = folderName + "/"+ Constants.PredictionWeightsFileName;
		mlog.say("writing in " + file);
		FileWriter writer;

		File theDir = new File(folderName);
		// if the directory does not exist, create it
		if (!theDir.exists()) {
			mlog.say("creating directory: " + folderName);
			boolean result = false;
			try{
				theDir.mkdirs();
				result = true;
			}
			catch(SecurityException se){
			}
			if(result) {
				System.out.println("DIR created");
			}
		}

		int written = 0;
		try {
			writer = new FileWriter(file);
			String str;

			//header 1
			str = "x,y,weight_value,weight_age\n";
			writer.write(str);
			writer.flush();

			str = x + "," + y + "," + value + "," + age + "\n";
			writer.write(str);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		mlog.say("wrote in " + file + " rows " + written);
	}

	/**
	 *
	 * @param values [from][to]
	 * @param ages
	 * @param delay
	 */
	public void writeSimplePredictionMatrix(int[][] values, int[][] ages, int delay, int maxWrite){
		mlog.say("writeSimplePredictionMatrix");
		String file = folderName + "/"+ Constants.PredictionWeightsFileName;
		mlog.say("writing in " + file);
		FileWriter writer;

		File theDir = new File(folderName);
		// if the directory does not exist, create it
		if (!theDir.exists()) {
			mlog.say("creating directory: " + folderName);
			boolean result = false;
			try{
				theDir.mkdirs();
				result = true;
			}
			catch(SecurityException se){
			}
			if(result) {
				System.out.println("DIR created");
			}
		}

		int written = 0;
		try {
			writer = new FileWriter(file);
			String str;

			//header 1
			str = "module_id,from_neuron,to_neuron,time_delay,weight_value,weight_age\n";
			writer.write(str);
			writer.flush();

			int maxSize = values[0].length;
			for (int from=0; from<maxSize; from++){
				for (int to=0; to<maxSize; to++){
					if(values[from][to]>0) {
						str = 0 + "," + from + "," + to + "," + delay + "," + values[from][to] + "," + ages[from][to] + "\n";
						writer.write(str);
						writer.flush();
						written++;
					}
					if(written>=maxWrite){
						break;
					}
				}
				if(written>=maxWrite){
					break;
				}
			}

			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		mlog.say("wrote in " + file + " rows " + written);
	}


	public void writeComposedPredictionMatrix(int[] nx, int[] ny, int grayscale, int fx, int fy, int[] filterValues,
											  int[] filterAges, int[][][] filters, int delay, int filterSize){
		mlog.say("writeComposedPredictionMatrix");

		String positionsFile = folderName + "/neurons_positions.csv";
		mlog.say("writing in " + positionsFile);
		FileWriter positionsWriter;

		File theDir = new File(folderName);
		// if the directory does not exist, create it
		if (!theDir.exists()) {
			mlog.say("creating directory: " + folderName);
			boolean result = false;
			try{
				theDir.mkdirs();
				result = true;
			}
			catch(SecurityException se){
			}
			if(result) {
				System.out.println("DIR created");
			}
		}

		try {
			positionsWriter = new FileWriter(positionsFile);
			String str;

			//header 1
			str = "neuron_id,x,y,grayscale\n";
			positionsWriter.write(str);
			positionsWriter.flush();
			for (int i = 0; i<nx.length; i++) {
                str = 0 + "," + nx[i] + "," + ny[i] + "," + grayscale + "\n";
                positionsWriter.write(str);
                positionsWriter.flush();
            }
            positionsWriter.close();

        } catch (IOException e) {
			e.printStackTrace();
		}

		String weightsFile = folderName + "/filter_weights.csv";
		mlog.say("writing in " + weightsFile);
		FileWriter weightsWriter;

		try {
			weightsWriter = new FileWriter(weightsFile);
			String str;

			//header 1
			str = "grayscale,neuronId,neuron_x,neuron_y," +
                    "filterId,filter_x,filter_y,value,age," +
                    "time_delay\n";
			weightsWriter.write(str);
			weightsWriter.flush();

			/*for (int filterId = 0; filterId<filterAges.length; filterId++) {
				str = grayscale + ",0," + filterId + "," + fx + "," + fy + "," + filterValues[filterId] + "," + filterAges[filterId]+ "," + delay + "\n";
				weightsWriter.write(str);
				weightsWriter.flush();
			}*/

			//go through all neurons for filter 1, then filter 2...
            for (int filterId = 0; filterId<filterAges.length; filterId++) {
                for (int nId = 0; nId < nx.length; nId++) {
                    int filterValueIndex = filterId*nx.length + nId;
                    str = grayscale + "," + nId + "," + nx[nId] + "," + ny[nId]
                            + "," + filterId + "," + fx + "," + fy + "," + filterValues[filterValueIndex] + "," + filterAges[filterId]
                            + "," + delay + "\n";
                    weightsWriter.write(str);
                    weightsWriter.flush();
                }
            }

			weightsWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String filtersFile = folderName + "/filters.csv";
		mlog.say("writing in " + filtersFile);
		FileWriter filtersWriter;
		try {
			filtersWriter = new FileWriter(filtersFile);
			String str;

			//header 1
			str = "grayscale,filterId,x,y,size\n";
			filtersWriter.write(str);
			filtersWriter.flush();

			for (int filterId = 0; filterId<filters.length; filterId++) {
				str = grayscale + "," + filterId + ",";
				for (int i=0; i<filters[0].length; i++){
					for (int j=0; j<filters[0].length; j++){
						if(filters[filterId][i][j]==1) {
							String secondary = str + j + "," + i + "," + filterSize + "\n";
							filtersWriter.write(secondary);
							filtersWriter.flush();
						}
					}
				}

			}
			filtersWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		mlog.say("finished writing");
	}

    public void writeFilterConfiguration(String direction, int fx, int fy, int filterSize, int grayscale, int errorMargin) {
        mlog.say("writeFilterConfiguration");
        String fileName = folderName+"/" + Constants.FilterConfigurationFileName;
        FileWriter writer;
        try {
            writer = new FileWriter(fileName);
            //header
            String str = "direction,fx,fy,filterSize,grayscale,error_margin";
            writer.write(str+"\n");
            writer.flush();
            str = direction + "," + fx + "," + fy + "," + filterSize + "," + grayscale + "," + errorMargin;
            writer.write(str+"\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

	public void writeFilterConfiguration(int nx, int ny, int fx, int fy, int filterSize, int grayscale, int errorMargin) {
		mlog.say("writeFilterConfiguration");
		String fileName = folderName+"/" + Constants.FilterConfigurationFileName;
		FileWriter writer;
		try {
			writer = new FileWriter(fileName);
			//header
			String str = "nx,ny,fx,fy,filterSize,grayscale,error_margin";
			writer.write(str+"\n");
			writer.flush();
			str = nx + "," + ny + "," + fx + "," + fy + "," + filterSize + "," + grayscale + "," + errorMargin;
			writer.write(str+"\n");
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
