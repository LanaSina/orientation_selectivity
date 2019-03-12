import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.Vector;

public class MatrixFilter {
    /**
     * current image
     */
    static int img_id = 0;
    static MyLog myLog = new MyLog("MatrixFilter", true);
    static int minimumActivations = 10;//10

    static int HORIZONTAL_DIRECTION = 0;
    static int VERTICAL_DIRECTION = 1;

    static int prediction_type = Constants.PixelVelocityPrediction;//Constants.FilterPrediction;//.SinglePixelPrediction;
    static int config = VisionConfiguration.FPSI;//OSWALD_20FPS;//OSWALD_SMALL_20FPS;
    static int input_type = Constants.ContrastInput;

    static int timeDelay = 1;
    //number of samples (positions at which the filters will be tested
    static int N = 1;


    public static void main(String[] args) {
        switch (prediction_type){
            case Constants.FilterPrediction:{
                filterPrediction();
                break;
            }
            case Constants.FilterToFilterPrediction:{
                filterToFilterPrediction();
                break;
            }
            case Constants.PixelVelocityPrediction:{
                pixelVelocityPrediction();
                break;
            }
            case Constants.SinglePixelPrediction:{
                switch (input_type) {
                    case Constants.GreyscaleInput: {
                        int[] greyscales = {0,9};//example of greyscales

                        for (int i = 0; i < greyscales.length; i++) {
                            //NAN results indicate that the selected greyscale never appeared in the selected dataset
                            singlePixelPrediction(greyscales[i]);
                        }
                        break;
                    }
                    case Constants.ContrastInput: {

                        break;
                    }
                }
                break;
            }
        }
    }

    //Predictions from one neuron to another

    /**
     * only looks at motion towards the right
     */
    private static void pixelVelocityPrediction(){
        MyLog myLog = new MyLog("pixelVelocityPrediction", true);
        //to read images
        VisionConfiguration configuration = new VisionConfiguration(config);
        int x = configuration.w/2;
        Random random = new Random();
        int y = random.nextInt(configuration.h);

        //to write results
        DataWriter dataWriter = new DataWriter(Constants.DataPath + "velocity_prediction/pixels/" +
                configuration.getConfigurationName()
                + "/t" + timeDelay, configuration);

        Eye eye = new Eye(configuration);
        //size of weight matrix
        int neuronsByGrayscale = eye.getNeuronsByGrayscale();
        myLog.say("size " + neuronsByGrayscale);
        img_id = configuration.start_number;

        //values
        int weightValue = 0;
        //ages
        int weightAge = 0;

        Vector<int[]> previousImages = new Vector<>();

        //training
        boolean shouldRun = true;
        while (shouldRun){

            //read image
            String iname = getImagePath(configuration);
            if(img_id<=configuration.n_images){
                eye.readImage(iname);
            }

            eye.preProcessInput();
            //square filled with current grayscale value
            int[] currentImage = eye.getCoarse();

            myLog.say("img_id " + img_id);
            if (img_id >= configuration.n_images){
                shouldRun = false;
                myLog.say("set run to false");
            }

            previousImages.add(currentImage);
            if (previousImages.size()>2) {//need to have 2 images buffered in, + current one
                //get greyscale value
                //extract input at location
                int[] previousPreviousImage = previousImages.remove(0);
                int[][] previousPreviousinput = getSquareFromFlat(previousPreviousImage, configuration.h / configuration.e_res, configuration.w / configuration.e_res);
                int greyscale = previousPreviousinput[y][x];

                //look for same value on the left side
                int[] previousImage = previousImages.get(0);
                int[][] previousInput = getSquareFromFlat(previousImage, configuration.h / configuration.e_res, configuration.w / configuration.e_res);
                int prevX = -1;
                for (int scanX = 0; scanX<x; scanX++) {
                    if(previousInput[y][scanX] == greyscale){
                        prevX = scanX;
                    }
                }
                //same value was not found
                if(prevX<0) continue;

                //same value was found
                //look if we can predict next image
                myLog.say("update age and value");
                weightAge++;
                int nextX = x + (x = prevX);
                int[][] input = getSquareFromFlat(currentImage, configuration.h / configuration.e_res, configuration.w / configuration.e_res);
                if(previousInput[y][nextX] == greyscale){
                    weightValue++;
                }
            }
            previousImages.add(currentImage);
        }

        dataWriter.writeSimplePrediction(weightValue,weightAge,x,y);
    }




    //Predictions from one filter to other filters
    /**
     **/
    private static void filterToFilterPrediction() {
        //to read images
        VisionConfiguration configuration = new VisionConfiguration(config);
        Eye eye = new Eye(configuration);
        //size of weight matrix
        int neuronsByGrayscale = eye.getNeuronsByGrayscale();
        int nGrayscales = configuration.gray_scales;
        int size = neuronsByGrayscale * nGrayscales;
        myLog.say("size " + size);
        img_id = configuration.start_number;

        int filterSize = 3;

        //horizontal, vertical, and full
        int filtersCount = 2;
        int[][][] filters = new int[filtersCount][filterSize][filterSize];
        filters[0][1][0] = 1;
        filters[0][1][1] = 1;
        filters[0][1][2] = 1;

        filters[1][0][1] = 1;
        filters[1][1][1] = 1;
        filters[1][2][1] = 1;

        displayFilters(filters);

        //total number of passes where filters have been activated at least once
        int n = 0;
        while(n<N){
            Random random = new Random();
            //avoid the black frame
            int offset = 0;//20
            //12 22
            int fx = random.nextInt(configuration.w- filterSize - offset*2) + offset;
            int fy = random.nextInt(configuration.h-filterSize - offset*2) + offset;

            switch (input_type) {
                case Constants.GreyscaleInput: {
                    /*int greyscale = random.nextInt(configuration.gray_scales);
                    String folderName = Constants.DataPath + "filter_to_filter_prediction/greyscale/" + configuration.getConfigurationName()
                            + "/t" + timeDelay +"/";
                    boolean nonZeroWeights = processFilters(timeDelay, filters, filtersCount, fx, fy, greyscale, filterSize, VERTICAL_DIRECTION,
                            offset, configuration, eye, folderName + "vertical/");
                    if(nonZeroWeights){
                        processFilters(timeDelay, filters, filtersCount, fx, fy, greyscale, filterSize, HORIZONTAL_DIRECTION,
                                offset, configuration, eye, folderName + "horizontal/");
                        n++;
                    }*/

                    break;
                }
                case Constants.ContrastInput: {
                    int contrast = 1;
                    if (random.nextBoolean()) {
                        contrast = -1;
                    }
                    String folderName = Constants.DataPath + "filter_to_filter_prediction/contrast/" + configuration.getConfigurationName()
                            + "/t" + timeDelay +"/";
                    boolean nonZeroWeights = processFilterToFilter(timeDelay, filters, filtersCount, fx, fy, contrast, filterSize, VERTICAL_DIRECTION,
                            offset, configuration, eye, folderName + "vertical/");
                    if(nonZeroWeights){
                        processFilterToFilter(timeDelay, filters, filtersCount, fx, fy, contrast, filterSize, HORIZONTAL_DIRECTION,
                                offset, configuration, eye, folderName + "horizontal/");
                        n++;
                    }

                    break;
                }
            }

        }
    }


    private static boolean processFilterToFilter(int timeDelay, int[][][] filters, int filtersCount,
                                          int fx, int fy, int greyscale, int filterSize, int direction, int offset,
                                          VisionConfiguration configuration, Eye eye,
                                          String folderName){

        int fGrayscale = greyscale;
        myLog.say("fx " + fx + " fy " + fy + " g " + fGrayscale);
        Vector<int[]> previousImages = new Vector<>();
        int errorMargin = 0;

        //if going left to right
        int maxDistance = 0;
        if(direction == HORIZONTAL_DIRECTION) {
            maxDistance = configuration.w / configuration.e_res - 2*offset - filterSize;
        } else if(direction == VERTICAL_DIRECTION) {
            maxDistance = configuration.h / configuration.e_res - 2*offset - filterSize;
        }

        int[] filterAges = new int[filtersCount];
        int[] filterValues = new int[filtersCount*maxDistance];
        int[] nxs = new int[maxDistance-offset];
        int[] nys = new int[maxDistance-offset];

        int nx = fx + filterSize/2;
        int ny = fy + filterSize/2;
        for (int i = 0; i<maxDistance; i++){

            if(direction==HORIZONTAL_DIRECTION){
                nx = i + offset + filterSize/2;
            } else if(direction==VERTICAL_DIRECTION){
                ny = i + offset + filterSize/2;
            }

            nxs[i] = nx;
            nys[i] = ny;
        }

        //training
        boolean shouldRun = true;
        while (shouldRun) {
            //read image
            String iname = getImagePath(configuration);
            if (img_id <= configuration.n_images) {
                eye.readImage(iname);
            }

            eye.preProcessInput();
            //square filled with current grayscale values
            int[] currentImage = eye.getCoarse();

            myLog.say("img_id " + img_id);
            if (img_id > configuration.n_images) {
                shouldRun = false;
            }

            //which filters were activated
            if (previousImages.size()>= timeDelay) {
                //extract input at filter location
                int[] previousImage = previousImages.remove(0);
                int[][] previousInput = getSquareFromFlat(previousImage, configuration.h / configuration.e_res, configuration.w / configuration.e_res);
                if(input_type == Constants.ContrastInput) {
                    previousInput = getContrast(previousInput);
                    //displayContrastImage(configuration.w, configuration.h, previousInput);
                    //return true;
                }
                //displayFilter(previousInput);

                int[][] subInput = getInputFrom(previousInput, fx, fy, filterSize);
                //displayFilter(subInput);
                int[] filterActivations = fillAges(filterAges, fGrayscale, filters, subInput, errorMargin);
                int[][] currentInput = getSquareFromFlat(currentImage, configuration.h / configuration.e_res, configuration.w / configuration.e_res);
                if(input_type == Constants.ContrastInput) {
                    currentInput = getContrast(currentInput);
                }
                //displayFilter(currentInput);
                fillFiltersValues(filters, filterActivations, filterValues, filterSize, currentInput,
                        nxs, nys, fGrayscale);
            }

            previousImages.add(currentImage);
        }

        img_id = configuration.start_number;
        myLog.say("ages " + filterAges[0] + " " + filterAges[1]);
        if(filterAges[0]<minimumActivations && filterAges[1]<minimumActivations){
            return false;
        }

        DataWriter dataWriter = new DataWriter(folderName + "x_" + fx + "_y_" + fy, configuration);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        //write configuration
        String ds = "horizontal";
        if(direction == VERTICAL_DIRECTION) {
            ds = "vertical";
        }
        dataWriter.writeFilterConfiguration(ds, fx, fy, filterSize, fGrayscale, errorMargin);
        //allow writing
        dataWriter.writeConfiguration(folderName);
        //write results
        dataWriter.writeComposedPredictionMatrix(nxs, nys, fGrayscale, fx, fy, filterValues, filterAges, filters, 1, filterSize);
        return true;
    }


    //Predictions from one filter to increasingly far away neurons
    /**
     *
     */
    private static void filterPrediction() {
        //to read images
        VisionConfiguration configuration = new VisionConfiguration(config);
        Eye eye = new Eye(configuration);
        //size of weight matrix
        int neuronsByGrayscale = eye.getNeuronsByGrayscale();
        int nGrayscales = configuration.gray_scales;
        int size = neuronsByGrayscale * nGrayscales;
        myLog.say("size " + size);
        img_id = configuration.start_number;

        int filterSize = 3;//3


        //horizontal, vertical, and full
        int filtersCount = 2;
        int[][][] filters = new int[filtersCount][filterSize][filterSize];
        filters[0][1][0] = 1;
        filters[0][1][1] = 1;
        filters[0][1][2] = 1;
        //add lines for bigger filters
//        filters[0][2][3] = 1;
//        filters[0][2][4] = 1;


        filters[1][0][1] = 1;
        filters[1][1][1] = 1;
        filters[1][2][1] = 1;

        //random filter
        /*
        100
        101
        000
         */
        /*filters[2][1][0] = 1;
        filters[2][0][0] = 1;
        filters[2][1][2] = 1;

        //full filter
        for(int i=0; i<3; i++){
            for (int j=0; j<3; j++){
                filters[3][i][j] = 1;
            }
        }*/
        displayFilters(filters);


        //total number of passes where all filters have been activated at least once
        int n = 0;
        //for (int i=0; i<10; i++) {
        while(n<N){
            Random random = new Random();
            //avoid the black frame
            int offset = 20;//20
            //12 20
            int fx = random.nextInt(configuration.w- filterSize - offset*2) + offset;
            int fy = random.nextInt(configuration.h-filterSize - offset*2) + offset;

            switch (input_type) {
                case Constants.GreyscaleInput: {
                    int greyscale = random.nextInt(configuration.gray_scales);

                    //myLog.say("fx " + fx + " fy " + fy + " greyscale " + greyscale);
                    String folderName = Constants.DataPath + "filter_prediction/greyscale/" + configuration.getConfigurationName()
                            + "/t" + timeDelay +"/";
                    boolean nonZeroWeights = processFilters(timeDelay, filters, filtersCount, fx, fy, greyscale, filterSize, VERTICAL_DIRECTION,
                            offset, configuration, eye, folderName + "vertical/");
                    if(nonZeroWeights){
                        processFilters(timeDelay, filters, filtersCount, fx, fy, greyscale, filterSize, HORIZONTAL_DIRECTION,
                                offset, configuration, eye, folderName + "horizontal/");
                        n++;
                    }

                    break;
                }
                case Constants.ContrastInput: {
                    //not greyscale, but contrast: 0,-1 or 1
                    int contrast = 1;
                    if (random.nextBoolean()) {
                        contrast = -1;
                    }//*/
                    myLog.say("fx " + fx + " fy " + fy + " contrast " + contrast);

                    String folderName = Constants.DataPath + "filter_prediction/contrast/" + configuration.getConfigurationName()
                            + "/t" + timeDelay +"/";
                    boolean nonZeroWeights = processFilters(timeDelay, filters, filtersCount, fx, fy, contrast, filterSize, VERTICAL_DIRECTION,
                            offset, configuration, eye, folderName + "vertical/");
                    if(nonZeroWeights){
                        processFilters(timeDelay, filters, filtersCount, fx, fy, contrast, filterSize, HORIZONTAL_DIRECTION,
                                offset, configuration, eye, folderName + "horizontal/");
                        n++;
                    }

                    break;
                }
            }

        }
    }


    /**
     * Process several filters for every neuron on the direction indicated
     * @param filters
     * @param filtersCount
     * @param fx
     * @param fy
     * @param greyscale
     * @param filterSize
     * @param direction
     * @param configuration
     * @param eye
     */
    private static boolean processFilters(int timeDelay, int[][][] filters, int filtersCount,
                                       int fx, int fy, int greyscale, int filterSize, int direction, int offset,
                                       VisionConfiguration configuration, Eye eye,
                                       String folderName){

        int fGrayscale = greyscale;
        myLog.say("fx " + fx + " fy " + fy + " g " + fGrayscale);
        //image at t-1
        //int[] previousImage = null;
        Vector<int[]> previousImages = new Vector<>();
        //int[] previousPreviousImage = null;
        int errorMargin = 0;

        //if going left to right
        int maxDistance = 0;
        if(direction == HORIZONTAL_DIRECTION) {
            maxDistance = configuration.w / configuration.e_res - 2*offset;
        } else if(direction == VERTICAL_DIRECTION) {
            maxDistance = configuration.h / configuration.e_res - 2*offset;
        }

        int[] filterAges = new int[filtersCount];
        int[] filterValues = new int[filtersCount*maxDistance];
        int[] nxs = new int[maxDistance-offset];
        int[] nys = new int[maxDistance-offset];

        //the prediction will be on the axis +,- width
        //int width = 1;
        int nx = fx + filterSize/2;
        int ny = fy + filterSize/2;
        for (int i = 0; i<maxDistance; i++){

            if(direction==HORIZONTAL_DIRECTION){
                nx = i + offset;
            } else if(direction==VERTICAL_DIRECTION){
                ny = i + offset;
            }

            nxs[i] = nx;
            nys[i] = ny;
        }

        //training
        boolean shouldRun = true;
        while (shouldRun) {
            //read image
            String iname = getImagePath(configuration);
            if (img_id <= configuration.n_images) {
                eye.readImage(iname);
            }


            eye.preProcessInput();
            //square filled with current grayscale values
            int[] currentImage = eye.getCoarse();

            myLog.say("img_id " + img_id);
            if (img_id > configuration.n_images) {
                shouldRun = false;
                myLog.say("set run to false");
            }

            //count which filters are currently activated
            if (previousImages.size()>= timeDelay) {//previousImage
                //extract input at filter location
                int[] previousImage = previousImages.remove(0);
                int[][] previousInput = getSquareFromFlat(previousImage, configuration.h / configuration.e_res, configuration.w / configuration.e_res);
                if(input_type == Constants.ContrastInput) {
                    previousInput = getContrast(previousInput);
                    //displayContrastImage(configuration.w, configuration.h, previousInput);
                    //return true;
                }
                //displayFilter(contrast);
                int[][] subInput = getInputFrom(previousInput, fx, fy, filterSize);//getInputFrom(input, fx, fy, filterSize);
                //displayFilter(subInput);
                int[] filterActivations = fillAges(filterAges, fGrayscale, filters, subInput, errorMargin);

                int[][] currentInput = getSquareFromFlat(currentImage, configuration.h / configuration.e_res, configuration.w / configuration.e_res);
                //displayFilter(input);
                if(input_type == Constants.ContrastInput) {
                    currentInput = getContrast(currentInput);
                }
                fillValues(filterActivations, filterValues, currentInput, nxs, nys, fGrayscale, errorMargin);
            }

            previousImages.add(currentImage);
            //previousPreviousImage = previousImage;
            //previousImage = currentImage;
        }

        img_id = configuration.start_number;

        myLog.say("ages " + filterAges[0] + " " + filterAges[1]);
        if(filterAges[0]<minimumActivations && filterAges[1]<minimumActivations){
            return false;
        }

        DataWriter dataWriter = new DataWriter(folderName + "x_" + fx + "_y_" + fy, configuration);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        //write configuration
        String ds = "horizontal";
        if(direction == VERTICAL_DIRECTION) {
            ds = "vertical";
        }
        dataWriter.writeFilterConfiguration(ds, fx, fy, filterSize, fGrayscale, errorMargin);
        //allow writing
        dataWriter.writeConfiguration(folderName);
        //write results
        dataWriter.writeComposedPredictionMatrix(nxs, nys, fGrayscale, fx, fy, filterValues, filterAges, filters, 1, filterSize);

        return true;
    }

    private static void displayContrastImage(int w, int h, int[][] data){

        final BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D)img.getGraphics();
        for(int i = 0; i < h; i++) {
            for(int j = 0; j < w; j++) {
                //0, 1, 2
                int d = (data[i][j] + 1) * 255/2;
                //myLog.say( " " + data[i][j] + " " + d);
                g.setColor(new Color(d, d, d));
                g.fillRect(i, j, 1, 1);
                //data[i][j] = r.nextDouble();
            }
        }

        JFrame frame = new JFrame("Image test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D)g;
                g2d.clearRect(0, 0, getWidth(), getHeight());
                g2d.setRenderingHint(
                        RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                // Or _BICUBIC
                g2d.scale(2, 2);
                g2d.drawImage(img, 0, 0, this);
            }
        };
        panel.setPreferredSize(new Dimension(w*2, h*2));
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
    }


    private static int maxValue(int[] input){
        int max = 0;
        for (int i =0; i <input.length; i++){
            if(input[i]>max){
                max = input[i];
            }
        }
        return max;
    }

    /**
     *
     * @param input
     * @return array, cell=-1 if paler than surrounding cells, 1 if darker, 0 if same
     */
    private static int[][] getContrast(int[][] input){
        int[][] contrast = new int[input.length][input[0].length];
        for (int i = 0; i< input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                double neighborhood = getNeighborhood(input, i, j);
                if(input[i][j]<neighborhood){
                    contrast[i][j] = -1;
                } else if(input[i][j]>neighborhood){
                    contrast[i][j] = 1;
                }
            }
        }
        return contrast;
    }

    /**
     *
     * @param input
     * @param i
     * @param j
     * @return the cropped mean of the grayscale value of the neighbors around i,j
     */
    private static int getNeighborhood(int[][] input, int i, int j){
        double mean = 0;
        int total = 0;
        for (int ii = i-1; ii<i+2; ii++) {
            for (int jj = j-1; jj<j+2; jj++) {
                if(jj!=j || ii!=i){
                    try {
                        mean+=input[ii][jj];
                        total++;
                    } catch (ArrayIndexOutOfBoundsException e){
                        //do nothing
                    }
                }
            }
        }
        mean = mean/total;
        return (int) mean;
    }

    /**
     *
     * @param filters
     */
    private static void displayFilters(int[][][] filters) {
        for (int f = 0; f< filters.length; f++){
            int[][] filter = filters[f];

            displayFilter(filter);
        }
    }

    private static void displayFilter(int[][] filter) {
        System.out.print("\n ---- \n");

        for (int i = 0; i< filter.length; i++){
            for (int j = 0; j< filter[0].length; j++){
                System.out.print(" " + filter[i][j]);
            }
            System.out.print("\n");
        }

    }

    /**
     *
     * @param filterActivations
     * @param filterValues
     * @param image current image
     * @param nxs
     * @param nys
     * @param filterGreyscale
     */
    private static void fillFiltersValues(int[][][] filters, int[] filterActivations, int[] filterValues, int filterSize, int[][] image,
                                   int[] nxs, int[] nys, int filterGreyscale) {

        //do all positions for a filter then the next filter
        for (int filterIndex = 0; filterIndex < filterActivations.length; filterIndex++) {
            int[][] filter = filters[filterIndex];

            if (filterActivations[filterIndex] == 1) {
                for (int positionIndex = 0; positionIndex<nxs.length; positionIndex++) {

                    int[][] movedInput = getInputFrom(image, nxs[positionIndex] - filterSize/2,
                            nys[positionIndex] - filterSize/2, filterSize);
                    int[][] inputGrayscale = getGrayscale(filterGreyscale, movedInput, 0);
                    //displayFilter(inputGrayscale);

                    boolean activated = filterActivated(filter, filterSize, inputGrayscale);
                    if (activated) {
                        int filterValueIndex = nxs.length * filterIndex + positionIndex;
                        filterValues[filterValueIndex] = filterValues[filterValueIndex] + 1;
                    }
                }
            }
        }
    }

    /**
     * if the selected neurons are activated, and their filters are activated, increase value
     * @param filterActivations
     * @param filterValues
     * @param image current image
     * @param neuronGrayscale
     */
    private static void fillValues(int[] filterActivations, int[] filterValues, int[][] image,
                                   int[] nxs, int[] nys, int neuronGrayscale, int errorMargin) {

       //do one filter for all ks, then the next filter
        for (int filterIndex = 0; filterIndex < filterActivations.length; filterIndex++) {
            if (filterActivations[filterIndex] == 1) {
                for (int neuronIndex = 0; neuronIndex<nxs.length; neuronIndex++) {
                    int neuronValue = image[nys[neuronIndex]][nxs[neuronIndex]];
                    //myLog.say("image[neuronK] " + image[neuronK] + " neuronGrayscale " + neuronGrayscale);
                    if ((neuronGrayscale >= neuronValue - errorMargin)
                            && (neuronGrayscale <= neuronValue + errorMargin)) {

                        int filterValueIndex = nxs.length * filterIndex + neuronIndex;
                        filterValues[filterValueIndex] = filterValues[filterValueIndex] + 1;
                    }
                }
            }
        }
    }



    /**
     * adds +1 to the filter's age if it is activated by this input
     * return activations [0 or 1]
     * @param filterAges
     * @param filterGrayscale the grayscale of each filter
     * @param filters
     * @param input current input
     */
    private static int[] fillAges(int[] filterAges, int filterGrayscale, int[][][] filters, int[][] input, int errorMargin) {
        int count = filterAges.length;
        int filterSize = filters[0].length;
        int[] activation = new int[filterAges.length];
        //get the input for the value of grayscale
        int[][] inputGrayscale = getGrayscale(filterGrayscale, input, errorMargin);
        //displayFilter(inputGrayscale);

        //check that the correct cells are activated
        for (int filterIndex = 0; filterIndex<count; filterIndex++){
            int[][] filter = filters[filterIndex];
            boolean activated = filterActivated(filter, filterSize, inputGrayscale);

            if(activated) {
                filterAges[filterIndex] += 1;
                activation[filterIndex] += 1;
            }
        }

        return activation;
    }

    /**
     *
     * @param filter
     * @param filterSize
     * @param input
     * @return true if the filter is activated by this input
     */
    private static boolean filterActivated(int[][] filter, int filterSize, int[][] input){
        boolean activated = true;

        int activeCells = 0;
        //to check that the correct cells are INactivated
        int[][] inverseFilter = new int[filterSize][filterSize];
        for (int i = 0; i < filterSize; i++) {
            for (int j = 0; j < filterSize; j++) {
                if (filter[i][j]==0) {
                    inverseFilter[i][j] = 1;
                } else {
                    activeCells++;
                }
            }
        }

        //will be == count(filter active cells) if input activates filter
        int sum = totalSum(filter,input);
        if(sum!=activeCells){
            activated = false;
        }

        sum = totalSum(inverseFilter,input);
        if(sum>0){
            activated = false;
        }

        return activated;
    }


    private static int[][] getGrayscale(int filterGrayscale, int[][] input, int errorMargin) {
        int[][] result = new int[input.length][input[0].length];
        for (int i = 0; i<input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                int g = input[i][j];
                if((g>=filterGrayscale-errorMargin) && (g<=filterGrayscale+errorMargin)){
                    result[i][j] = 1;
                }
            }
        }

        return result;
    }

    /**
     * multiplies a * b elementwise and sums all rows and columns
     * @param a
     * @param b
     * @return
     */
    private static int totalSum(int[][] a, int[][] b) {

        int arows = a.length;
        int acols = a[0].length;
        int sum = 0;

        for (int arow = 0; arow<arows; arow++) {
            for (int acol = 0; acol < acols; acol++) {
                sum += a[arow][acol] * b[arow][acol];
            }
        }

        return sum;
    }

    /**
     *
     * @param input 1d
     * @return 2d [rows][cols]
     */
    private static int[][] getSquareFromFlat(int[] input, int height, int width) {


        Vector<Integer> indices = new Vector<>();
        Vector<Integer> dimensions = new Vector<>();
        dimensions.add(height);
        dimensions.add(width);
        indices.add(0);
        indices.add(0);

        int[][] square = new int[height][width];
        for (int row = 0; row<height; row++){
            indices.set(0,row);
            for (int col = 0; col<width; col++){
                indices.set(1,col);
                //int i = row*width + col;
                square[row][col] = getFromArray(indices,dimensions,input);
            }
        }

        //displayFilter(square);

        return square;
    }

    /**
     *
     * @param input 2d array [row][col]
     * @param x subset start on x
     * @param y y downwards oriented
     * @param size subset size
     * @return a square subset of the original array
     */
    private static int[][] getInputFrom(int[][] input, int x, int y, int size) {
        int[][] subset = new int[size][size];

        for (int row = 0; row<size; row++){
            for (int col = 0; col<size; col++){
                subset[row][col] = input[y+row][x+col];
            }
        }

        //displayFilter(subset);

        return subset;
    }


    /**
     *
     * @param indices row, col
     * @param dimensions eg height, width
     * @param multidimArray
     * @return
     */
    private static int getFromArray(Vector<Integer> indices, Vector<Integer> dimensions, int[] multidimArray) {
        int size = indices.size();
        int index = 0;
        for (int i=0; i<size-1; i++){
            index += indices.get(i)*dimensions.get(i+1);//eg y*width
        }

        index += indices.get(size-1); //eg += x

        return multidimArray[index];
    }


    //Predictions from one neuron to another
    private static void singlePixelPrediction(int greyscale){
        MyLog myLog = new MyLog("singlePixelPrediction", true);

        //to read images
        VisionConfiguration configuration = new VisionConfiguration(config);

        //to write results
        DataWriter dataWriter = new DataWriter(Constants.DataPath + "single_pixel_prediction/" +
                configuration.getConfigurationName()
                + "/t" + timeDelay + "_" + greyscale, configuration);

        Eye eye = new Eye(configuration);
        //size of weight matrix
        int neuronsByGrayscale = eye.getNeuronsByGrayscale();
        myLog.say("size " + neuronsByGrayscale);
        img_id = configuration.start_number;

        //weight matrix i->j, rows->col
        //values
        int[][] weightValues = new int[neuronsByGrayscale][neuronsByGrayscale];
        //ages
        int[][] weightAges = new int[neuronsByGrayscale][neuronsByGrayscale];

        Vector<int[]> previousImages = new Vector<>();

            //training
        boolean shouldRun = true;
        while (shouldRun){

            //read image
            String iname = getImagePath(configuration);
            if(img_id<=configuration.n_images){
                eye.readImage(iname);
            }

            eye.preProcessInput();
            //square filled with current grayscale value
            int[] currentImage = eye.getCoarse();

            myLog.say("img_id " + img_id);
            if (img_id >= configuration.n_images){
                shouldRun = false;
                myLog.say("set run to false");
            }

            if (img_id < configuration.n_images) {
                myLog.say("update ages");

                for (int k = 0; k < neuronsByGrayscale; k++) {
                    int grayscale_k = currentImage[k];
                    if(grayscale_k!=greyscale){
                        continue;
                    }
                    // matrix structure: [from,to]
                    //0...n one grayscale, row by row
                    // etc until go to next row
                    //int i = grayscale_i * neuronsByGrayscale + k;

                    //age weights from k to all other cells
                    for (int col = 0; col < neuronsByGrayscale; col++) {
                        weightAges[k][col] = weightAges[k][col] + 1;
                    }
                }
            }


            if(previousImages.size()>= timeDelay){ //previousImage!=null) {
                myLog.say("update values");
                int[] previousImage = previousImages.remove(0);

                for (int k = 0; k < neuronsByGrayscale; k++) {
                    int grayscale_k = previousImage[k];
                    //int i = grayscale_i * neuronsByGrayscale + k;
                    if(grayscale_k!=greyscale){
                        continue;
                    }

                    for (int l = 0; l < neuronsByGrayscale; l++) {
                        int grayscale_l = currentImage[l];

                        if(grayscale_l!=greyscale){
                            continue;
                        }

                        //int j = grayscale_j * neuronsByGrayscale + l;

                        //increase value from all k to all l
                        //if (img_id <= configuration.n_images) {
                            weightValues[k][l] = weightValues[k][l] + 1;
                        //}
                    }
                }
            }

            previousImages.add(currentImage);
        }

        int maxSize = neuronsByGrayscale*configuration.gray_scales;
        maxSize = maxSize*maxSize;
        dataWriter.writeSimplePredictionMatrix(weightValues,weightAges,1, maxSize);
    }


    private static String getImagePath(VisionConfiguration configuration) {

        String imagepath =  configuration.images_path + String.format(configuration.name_format, img_id) + configuration.image_format;

        //change image
        img_id++;
        return imagepath;
    }

}


