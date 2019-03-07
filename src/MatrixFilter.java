import java.math.BigInteger;
import java.util.Random;
import java.util.Vector;

public class MatrixFilter {
    /**
     * current image
     */
    static int img_id = 0;
    static MyLog myLog = new MyLog("MatrixFilter", true);

    static int HORIZONTAL_DIRECTION = 0;
    static int VERTICAL_DIRECTION = 1;

    static int prediction_type = Constants.SinglePixelPrediction;
    static int config = VisionConfiguration.KITTI;//OSWALD_20FPS;//OSWALD_SMALL_20FPS;

    public static void main(String[] args) {
        switch (prediction_type){
            case Constants.FilterPrediction:{
                oneFilterToManyNeurons();
                break;
            }
            case Constants.SinglePixelPrediction:{
                singlePixelPrediction();
                break;
            }
        }

        //singlePixelPrediction();
        //groupPredictionLoop();
    }

    //Predictions from one filter to increasingly far away neurons
    private static void oneFilterToManyNeurons() {
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
        filters[0][2][0] = 1;
        filters[0][2][1] = 1;
        filters[0][2][2] = 1;
        //add lines for bigger filters
//        filters[0][2][3] = 1;
//        filters[0][2][4] = 1;


        filters[1][0][2] = 1;
        filters[1][1][2] = 1;
        filters[1][2][2] = 1;
//        filters[1][3][2] = 1;
//        filters[1][4][2] = 1;

        //random filter
        /*
        100
        101
        000
         */
//        filters[2][1][0] = 1;
//        filters[2][0][0] = 1;
//        filters[2][1][2] = 1;
//
//        //full filter
//        for(int i=0; i<3; i++){
//            for (int j=0; j<3; j++){
//                filters[3][i][j] = 1;
//            }
//        }
        displayFilters(filters);


        for (int i=0; i<10; i++) {
            //int i = 1;
            Random random = new Random();
            //avoid the black frame
            int offset = 20;
            int fx = random.nextInt(configuration.w- filterSize - offset*2) + offset;
            int fy = random.nextInt(configuration.h-filterSize - offset*2) + offset;
            //not greyscale, but contrast: 0,-1 or 1
            int contrast = -1;
            if(random.nextBoolean()){
                contrast = 1;
            }
            myLog.say("fx " + fx + " fy " + fy + " contrast " + contrast);
            String subfolder = "filter_prediction/";

            String folderName = Constants.DataPath + subfolder + configuration.getConfigurationName()
                    + "/contrast_sliding_distance/";
            processFilters(filters, filtersCount, fx, fy, contrast, filterSize, HORIZONTAL_DIRECTION,
                    offset, configuration, eye, folderName + "horizontal_s5/" + i + "/");
            processFilters(filters, filtersCount, fx, fy, contrast, filterSize, VERTICAL_DIRECTION,
                    offset, configuration, eye, folderName + "vertical_s5/" + i + "/");

        }
    }

    //Predictions from several neurons to one other neuron
    private static void groupPredictionLoop() {

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
        int w = configuration.w/configuration.e_res;
        int h = configuration.h/configuration.e_res;
        int radius = 4;
        //leftmost edge of the filter compared to neuron
        int offsetX = - filterSize - 4;
        int offsetY = - (filterSize / 2);


        /*offsetX = filterSize;
        int nx = 17;
        int ny = 6;
        int grayscale = 9;
        processFilter(nx,ny,offsetX,offsetY,grayscale,filterSize,configuration,eye);*/

        //number of cells in filter -> 3 out of 9
        //ref https://math.stackexchange.com/questions/1525332/how-many-ways-can-i-choose-5-items-from-10
        int filtersCount = factorial(9) / (factorial(3) * factorial(9 - 3));
        myLog.say("filtersCount " + filtersCount);
        int[][][] filters = new int[filtersCount][filterSize][filterSize];

        //fill the filters
        int[] allOnes = new int[filterSize * 2];
        int[] startPoint = {0, 0};
        fillFilters(filters, startPoint, 0, allOnes, 0, filterSize);


        Random random = new Random();
        //for (int i = 0; i < 10; i++) {
        //process filters all around this one neuron (dataset is small, not all orientations can be represented?)
        String[] orientations = {"left", "diagonal", "top", "diagonal", "right", "diagonal", "bottom", "diagonal"};
        //random neuron position
        int nx = random.nextInt(w);
        int ny = random.nextInt(h);

        for(int orientation = 0; orientation<8; orientation++) {
            //offset
            switch (orientation){
                //left
                case 0:{
                    offsetX = -filterSize - radius;
                    offsetY = -(filterSize / 2);
                    break;
                }
                case 1:{
                    offsetX = -filterSize - radius;
                    offsetY = -filterSize - radius;
                    break;
                }
                //top
                case 2:{
                    offsetX = -(filterSize / 2);
                    offsetY = -filterSize - radius;
                    break;
                }
                case 3:{
                    offsetX = radius;
                    offsetY = -filterSize - radius;
                    break;
                }
                //right
                case 4:{
                    offsetX = radius;
                    offsetY = -(filterSize / 2);
                    break;
                }
                case 5:{
                    offsetX = radius;
                    offsetY = radius;
                    break;
                }
                //bottom
                case 6:{
                    offsetX = -(filterSize / 2);
                    offsetY = radius;
                    break;
                }
                case 7:{
                    offsetX = -filterSize - radius;
                    offsetY = radius;
                    break;
                }
            }


            int grayscale = random.nextInt(configuration.gray_scales);


            //skip impossible positions
            if(offsetX+nx>w && offsetY+ny>h){
                continue;
            }

            processFilters(filters, filtersCount, nx, ny, offsetX, offsetY, grayscale, filterSize, configuration, eye, orientations[orientation], 0);
            img_id = configuration.start_number;
        }
        //}
    }


    /**
     * Process several filters for every neuron on the direction indicated
     * @param filters
     * @param filtersCount
     * @param fx
     * @param fy
     * @param fGrayscale
     * @param filterSize
     * @param direction
     * @param configuration
     * @param eye
     */
    //filters, filtersCount, fx, fy, grayscale, filterSize, direction, configuration, eye
    private static void processFilters(int[][][] filters, int filtersCount,
                                       int fx, int fy, int fGrayscale, int filterSize, int direction, int offset,
                                       VisionConfiguration configuration, Eye eye,
                                       String folderName){

        myLog.say("fx " + fx + " fy " + fy + " g " + fGrayscale);
        //image at t-1
        int[] previousImage = null;
        int errorMargin = 0;

        DataWriter dataWriter = new DataWriter(folderName, configuration);

        //if going left to right
        int maxDistance = 0;
        if(direction == HORIZONTAL_DIRECTION) {
            maxDistance = configuration.w / configuration.e_res - offset;
        } else if(direction == VERTICAL_DIRECTION) {
            maxDistance = configuration.h / configuration.e_res - offset;
        }

        int[] filterAges = new int[filtersCount];
        int[] filterValues = new int[filtersCount*maxDistance];
        int[] nxs = new int[maxDistance-offset];
        int[] nys = new int[maxDistance-offset];

        int nx = fx + filterSize/2;
        int ny = fy + filterSize/2;
        for (int i = offset; i<maxDistance; i++){

            if(direction==HORIZONTAL_DIRECTION){
                nx = i;
            } else if(direction==VERTICAL_DIRECTION){
                ny = i;
            }

            nxs[i-offset] = nx;
            nys[i-offset] = ny;
            //neuronKs[i-offset] =  ny * (configuration.vf_w/ configuration.e_res) + nx;
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
            if (img_id >= configuration.n_images) {
                shouldRun = false;
                myLog.say("set run to false");
            }

            //count which filters are currently activated
            if (previousImage != null) {
                //extract input at filter location
                int[][] input = getSquareFromFlat(previousImage, configuration.h / configuration.e_res, configuration.w / configuration.e_res);
                //displayFilter(input);
                int[][] contrast = getContrast(input);
                //displayFilter(contrast);
                int[][] subInput = getInputFrom(contrast, fx, fy, filterSize);//getInputFrom(input, fx, fy, filterSize);
                //displayFilter(subInput);
                int[] filterActivations = fillAges(filterAges, fGrayscale, filters, subInput, errorMargin);
                //fillValues(filterActivations, filterValues, currentImage, neuronKs, fGrayscale, errorMargin); replace by input
                fillValues(filterActivations, filterValues, contrast, nxs, nys, fGrayscale, errorMargin);
            }

            previousImage = eye.getCoarse();
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
        img_id = configuration.start_number;
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


    //0. 1 neuron and its considered filter locations (2D squares)
    //1. Have a map of 2D coordinates of the considered filter locations
    //2. Each coordinate is mapped to a collection of all possible filters
    //3. That is obtained by: any combined activation of n cells constitutes a filter
    //   so the collection of all filters is a collection of 2D arrays with 0 and n*1 weights (so the collection is just a 3d array)
    //4. We go through the collection with cropped input image, checking activation by multiplying matrices and checking if == 1
    /**
     * @param nx
     * @param ny
     * @param offsetX
     * @param offsetY
     * @param fGrayscale considered greyscale
     * @param filterSize
     * @param configuration
     * @param eye
     * @param orientation where is the filter placed relative to the neuron
     * @param experimentIndex
     */
    private static void processFilters(int[][][] filters, int filtersCount,
                                       int nx, int ny, int offsetX, int offsetY, int fGrayscale, int filterSize,
                                        VisionConfiguration configuration, Eye eye, String orientation, int experimentIndex){


        //image at t-1
        int[] previousImage = null;
        int errorMargin = 0;
        int fx = nx + offsetX;
        int fy = ny + offsetY;

        //String subfolder = "nx" + nx + "_ny" + ny;
        String folderName = Constants.DataPath + "group_matrix_filters/" + configuration.getConfigurationName()
                + "/" + orientation + "/" + experimentIndex + "/";

        DataWriter dataWriter = new DataWriter(folderName, configuration);

        myLog.say("fy " + fy + " fx " + fx + " fGrayscale " + fGrayscale);

        //displayFilters(filters);
        //myLog.say("max filterId " + filterId);

        int[] filterAges = new int[filtersCount];
        int[] filterValues = new int[filtersCount];

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
            if (img_id >= configuration.n_images) {
                shouldRun = false;
                myLog.say("set run to false");
            }

            //count which filters are currently activated
            int[] nxs = {nx};
            int[] nys = {ny};
            if (previousImage != null) {
                //extract input at filter location
                int[][] input = getSquareFromFlat(previousImage, configuration.h / configuration.e_res, configuration.w / configuration.e_res);
                int[][] subInput = getInputFrom(input, fx, fy, filterSize);
                int[] filterActivations = fillAges(filterAges, fGrayscale, filters, subInput, errorMargin);
                //int[] neuronK = {ny * configuration.vf_h / configuration.e_res + nx};
                fillValues(filterActivations, filterValues, input, nxs, nys, fGrayscale, errorMargin);
            }

            previousImage = eye.getCoarse();
        }



        //write configuration
        dataWriter.writeFilterConfiguration(nx, ny, fx, fy, filterSize, fGrayscale, errorMargin);
        //allow writing
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        dataWriter.writeConfiguration(folderName);
        //allow writing
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //write results
        int[] nxs = {nx};
        int[] nys = {ny};
        dataWriter.writeComposedPredictionMatrix(nxs, nys, fGrayscale, fx, fy, filterValues, filterAges, filters, 1, filterSize);
    }

    private static int fillFilters(int[][][] filters, int[] endPoint, int filterId, int[] allOnes, int depth, int filterSize){

        int[] startPoint = {endPoint[0],endPoint[1]};
        int[] one = {0,0};

        //while all positions at this depth have not been tested
        while (one[0]!=-1){
            if(filterId==filters.length) return filterId;

            int[][] filter = filters[filterId];

            if(depth==filterSize-1) {
                //copy pattern up to this depth-1
                for (int d = 0; d < depth; d++) {
                    filter[allOnes[d * 2]][allOnes[d * 2 + 1]] = 1;
                }
            }

            //place a 1
            one = placeOne(filterSize, filters[filterId], startPoint);

            if(one[0]!=-1) {
                allOnes[depth*2] = one[0];
                allOnes[depth*2+1] = one[1];

                //calculate startpoint for next depth
                startPoint[0] = one[0];
                startPoint[1] = one[1] + 1;
                if (startPoint[1] == filterSize) {
                    startPoint[1] = 0;
                    startPoint[0] = startPoint[0] + 1;
                }

                if(depth==filterSize-1) {
                    //move to next filter
                    filterId++;
                } else {
                    //move to next depth
                    filterId = fillFilters(filters, startPoint, filterId, allOnes, depth+1, filterSize);
                }
            } else {
                if(depth==filterSize-1) {
                    //clean up failed filter
                    for (int d = 0; d < 2; d++) {
                        filter[allOnes[d * 2]][allOnes[d * 2 + 1]] = 0;
                    }
                }
            }
        }

        return filterId;
    }


    /**
     * puts a 1 in the first available place
     * @param size
     * @param filter
     * @return the coordinates of the added 1 or [-1,-1]
     */
    private static int[] placeOne(int size, int[][] filter, int[] startingPoint){
        int[] coordinates = {-1,-1};

        for(int i=startingPoint[0]; i<size; i++) {
            for (int j = startingPoint[1]; j < size; j++) {
                if(filter[i][j]==0){
                    filter[i][j] = 1;
                    coordinates[0] = i;
                    coordinates[1] = j;
                    return coordinates;
                }
            }
        }
        return coordinates;
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
     * if the selected neurons are activated, and their filters are activated, increase value
     * @param filterActivations
     * @param filterValues
     * @param image
     * @param neuronGrayscale
     */
    private static void fillValues(int[] filterActivations, int[] filterValues, int[][] image,
                                   int[] nxs, int[] nys, int neuronGrayscale, int errorMargin) {


       /* for (int neuronIndex = 0; neuronIndex<neuronKs.length; neuronIndex++) {
            int neuronK = neuronKs[neuronIndex];
            if ((neuronGrayscale >= image[neuronK] - errorMargin)
                    && (neuronGrayscale <= image[neuronK] + errorMargin)) {

                for (int filterIndex = 0; filterIndex < filterActivations.length; filterIndex++) {
                    if (filterActivations[filterIndex] == 1) {
                        filterValues[filterIndex] = filterValues[filterIndex] + 1;
                    }
                }
            }
        }*/

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
     * @param input
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
            int sum = totalSum(filter,inputGrayscale);
            if(sum!=activeCells){
                activated = false;
            }


            /*myLog.say("inverseFilter");
            displayFilter(inverseFilter);
            sum = totalSum(inverseFilter,inputGrayscale);
            myLog.say("sum " + sum);
            if(sum>0){
                activated = false;
            }*/

            if(activated) {
                filterAges[filterIndex] += 1;
                activation[filterIndex] += 1;
            }
        }

        return activation;
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
     * multiply two matrices a*b
     * @param a
     * @param b
     * @return
     */
    private static int[][] multiply(int[][] a, int[][] b) {
        int arows = a.length;
        int acols = a[0].length;
        int bcols = b[0].length;
        int[][] result = new int[arows][bcols];

        for (int arow = 0; arow<arows; arow++) {
            for (int bcol = 0; bcol < bcols; bcol++) {
                for (int acol = 0; acol < acols; acol++) {
                    result[arow][bcol] += a[arow][acol] * b[acol][bcol];
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
     * add +1 to currently activated filters in multidim flattened array of correlationMatrix
     * @param correlationMatrix
     * @param indices [neuron][filters] all flattened
     * @param dimensions
     * @param depth
     */
    private static void calculateFilterActivated(int[] correlationMatrix, int[] valueMatrix, int[] flattenedInputT0, int[] flattenedInputT1,
                                                 Vector<Integer> indices, Vector<Integer> dimensions, int depth, boolean calculateValues){

        int neuronIndex = indices.get(0);
        int maxIndex = indices.get(depth);
        depth = depth+1;
        for (int i = 0; i<maxIndex; i++){
            if(flattenedInputT1[i]==1){
                indices.set(depth,i);

                if(depth==indices.size()-1){
                    //this increases the age of the filter
                    int age = getFromArray(indices, dimensions, correlationMatrix);
                    setInArray(indices, dimensions, correlationMatrix, age+1);
                    //this increases its value
                    if(calculateValues && flattenedInputT0[neuronIndex]==1) {
                        int value = getFromArray(indices, dimensions, valueMatrix);
                        setInArray(indices, dimensions, valueMatrix, value + 1);
                    }
                } else {
                    calculateFilterActivated(correlationMatrix, valueMatrix, flattenedInputT0, flattenedInputT1,
                            indices, dimensions, depth, calculateValues);
                }
            }
        }
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

    private static void setInArray(Vector<Integer> indices, Vector<Integer> dimensions, int[] multidimArray, int value) {
        int size = indices.size();
        int index = 0;
        for (int i=0; i<size-1; i++){
            index += dimensions.get(i)*indices.get(i);
        }

        index += indices.get(indices.size()-1);

        multidimArray[index] = value;
    }



    //Predictions from one neuron to another
    private static void singlePixelPrediction(){
        MyLog myLog = new MyLog("singlePixelPrediction", true);

        //to read images
        VisionConfiguration configuration = new VisionConfiguration(config);
        Eye eye = new Eye(configuration);
        //size of weight matrix
        int neuronsByGrayscale = eye.getNeuronsByGrayscale();
        int nGrayscales = configuration.gray_scales;
        int size = neuronsByGrayscale* nGrayscales;
        myLog.say("size " + size);
        img_id = configuration.start_number;

        //weight matrix i->j, rows->col
        //values
        int[][] weightValues = new int[size][size];
        //ages
        int[][] weightAges = new int[size][size];
        //image at t-1
        int[] previousImage = null;

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
                    int grayscale_i = currentImage[k];//previousImage[k];

                    // matrix structure: [from,to]
                    //0...n one grayscale, row by row
                    // etc until go to next row
                    int i = grayscale_i * neuronsByGrayscale + k;

                    //age weights from i to all other cells
                    for (int row = 0; row < size; row++) {
                        weightAges[i][row] = weightAges[i][row] + 1;
                    }
                }
            }


            if(previousImage!=null) {
                myLog.say("update values");

                for (int k = 0; k < neuronsByGrayscale; k++) {
                    int grayscale_i = previousImage[k];
                    int i = grayscale_i * neuronsByGrayscale + k;

                    for (int l = 0; l < neuronsByGrayscale; l++) {
                        int grayscale_j = currentImage[l];
                        int j = grayscale_j * neuronsByGrayscale + l;

                        //increase value from all i to all k
                        if (img_id <= configuration.n_images) {
                            weightValues[i][j] = weightValues[i][j] + 1;
                        }
                    }
                }
            }

            previousImage  = eye.getCoarse();
        }

        //write results
        DataWriter dataWriter = new DataWriter(Constants.DataPath + "single_pixel_prediction/" +configuration.getConfigurationName(), configuration);
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

    public static int factorial(int n) {
        BigInteger number = BigInteger.valueOf(n);
        BigInteger result = BigInteger.valueOf(1);

        for (long factor = 2; factor <= number.longValue(); factor++) {
            result = result.multiply(BigInteger.valueOf(factor));
        }

        return result.intValue();
    }

}


