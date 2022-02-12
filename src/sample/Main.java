package sample;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.text.*;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

import static java.lang.System.exit;

public class Main extends Application {
    Button fileButton, nextButton, nextButton2, nextButton3, quitButton, quitButton2, quitButton3, refreshButton;
    Scene mainScene;
    LineChart<Number,Number> audioChart;
    NumberAxis cbXAxis;
    NumberAxis cbYAxis;
    XYChart.Series cbSeries;
    Scene linechartScene, imageScene;
    Collection<XYChart.Data<Integer,Short>> samples;
    int sceneNumber, refreshNumber;
    Text sceneText;
    VBox audioLayout;
    Image currentImage;
    HBox imageHBox;
    VBox imageLayout;
    String currentImagePath;
    String grayScaleImagePath;

    private int calcNumberOfFrames(File wavFile){
        int totalFramesRead = 0;
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(wavFile);
            int bytesPerFrame = audioInputStream.getFormat().getFrameSize();
            if (bytesPerFrame == AudioSystem.NOT_SPECIFIED) {
                bytesPerFrame = 1;
            }
            samples = new ArrayList<>();
            int numBytes = bytesPerFrame;
            byte[] audioBytes = new byte[numBytes];
            try {
                int numBytesRead;
                int numFramesRead;
                int flag = 1;//Using flag to skip over bytes to reduce time taken. Starts at 49 to skip header bytes.
                while ((numBytesRead = audioInputStream.read(audioBytes)) != -1) {
                    if(flag == 1){flag++;}
                    else if((flag >= 2) & (flag <= 49)){flag++;continue;}
                    else if(flag == 50){flag = 1; continue;}
                    // Calculate the number of frames actually read.
                    numFramesRead = numBytesRead / bytesPerFrame;
                    totalFramesRead += numFramesRead;
                }
                return totalFramesRead;
            } catch (Exception ex) {
                System.out.println("Problem reading audio bytes");
            }
        } catch (Exception e) {
            System.out.println("Could not read file to input stream");
        }
        return -1;
    }

    private void readWAVAndDraw(File wavFile){
        cbSeries = new XYChart.Series();
        int totalFramesRead = 0;
        int totalFrames = 0;
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(wavFile);
            if(sceneNumber == 2){
                totalFrames = calcNumberOfFrames(wavFile);
            }
            int bytesPerFrame = audioInputStream.getFormat().getFrameSize();
            if (bytesPerFrame == AudioSystem.NOT_SPECIFIED) {
                bytesPerFrame = 1;
            }
            samples = new ArrayList<>();
            int numBytes = bytesPerFrame;
            byte[] audioBytes = new byte[numBytes];
            try {
                int numBytesRead;
                int numFramesRead;
                int loopCounter = 1;
                short amplitude;
                int flag = 1;//Using flag to skip over bytes to reduce time taken. Starts at 49 to skip header bytes.
                while ((numBytesRead = audioInputStream.read(audioBytes)) != -1) {
                    if(flag == 1){flag++;}
                    else if((flag >= 2) & (flag <= 49)){flag++;continue;}
                    else if(flag == 50){flag = 1; continue;}
                    // Calculate the number of frames actually read.
                    numFramesRead = numBytesRead / bytesPerFrame;
                    totalFramesRead += numFramesRead;
                    amplitude = convertToShort(audioBytes);
                    if(sceneNumber == 2){
                        amplitude = fadeEffect(amplitude,totalFramesRead,totalFrames);
                    }
                    //amplitude = (short) (amplitude/50);//Dividing to make amplitude fit better
                    XYChart.Data sample = new XYChart.Data<>( loopCounter, amplitude);
                    samples.add(sample);
                    cbSeries.getData().add(new XYChart.Data(loopCounter,amplitude));
                    loopCounter++;
                }
                cbSeries.getData().addAll(samples);
            } catch (Exception ex) {
                System.out.println("Problem reading audio bytes");
            }
        } catch (Exception e) {
            System.out.println("Could not read file to input stream");
        }
    }

    @Override
    public void start(Stage window) throws Exception{
        sceneNumber = 1;
        refreshNumber = 1;
        //Initializing Chart for Cowbell WAV
        cbXAxis = new NumberAxis();
        cbXAxis.setLabel("Samples");
        cbYAxis = new NumberAxis();
        cbYAxis.setLabel("Amplitude");
        audioChart = new LineChart<>(cbXAxis, cbYAxis);
        sceneText = new Text(10,50,"Browse and select the audio file to view its waveform, without fade-in");
        sceneText.setFont(new Font(15));
        cbSeries = new XYChart.Series();
        cbSeries.setName("Cowbell Waveform Data");

        //Quit Button for each scene
        quitButton = new Button("Quit");
        quitButton.setOnAction(e-> exit(0));
        quitButton2 = new Button("Quit");
        quitButton2.setOnAction(e-> exit(0));
        quitButton3 = new Button("Quit");
        quitButton3.setOnAction(e-> exit(0));
        //Next Button for each scene - Exit Button for each scene
        nextButton = new Button("Next");
        nextButton.setOnAction(e->{
            window.setWidth(950);
            //Go to the next scene
            //We can identify the current scene usign a class variable which keeps it in mind
            //A switch clause can then handle it.
            switch (sceneNumber) {
                case 1 -> {
                    window.setScene(mainScene);
                    window.setTitle("Audio wav with fade-in and fade-out effect"); // Go to second scene
                    sceneText.setText("Browse and select the Audio file to view its waveform, with fade-in");
                    sceneNumber++;
                }
                case 2 -> {
                    refreshNumber = 1;
                    window.setScene(mainScene);
                    window.setTitle("BMP File Viewing");
                    sceneText.setText("Browse and select the image .bmp file to view");
                    sceneNumber++;
                }
                case 3 ->{
                    window.setScene(mainScene);
                    window.setTitle("Audio Waveform without fade-in and fade-out effect");
                    sceneText.setText("Browse and select the Audio file to view its waveform, without fade-in");
                    sceneNumber = 1;
                }

            }
        });
        nextButton2 = new Button("Next");
        nextButton2.setOnAction(e->{
            window.setWidth(950);
            //Go to the next scene
            //We can identify the current scene usign a class variable which keeps it in mind
            //A switch clause can then handle it.
            switch (sceneNumber) {
                case 1 -> {
                    window.setScene(mainScene);
                    window.setTitle("Audio wav with fade-in and fade-out effect"); // Go to second scene
                    sceneText.setText("Browse and select the Audio file to view its waveform, with fade-in");
                    sceneNumber++;
                }
                case 2 -> {
                    refreshNumber = 1;
                    window.setScene(mainScene);
                    window.setTitle("BMP File Viewing");
                    sceneText.setText("Browse and select the image .bmp file to view");
                    sceneNumber++;
                }
                case 3 ->{
                    window.setScene(mainScene);
                    window.setTitle("Audio Waveform without fade-in and fade-out effect");
                    sceneText.setText("Browse and select the Audio file to view its waveform, without fade-in");
                    sceneNumber = 1;
                }

            }
        });
        nextButton3 = new Button("Next");
        nextButton3.setOnAction(e->{
            window.setWidth(950);
            //Go to the next scene
            //We can identify the current scene usign a class variable which keeps it in mind
            //A switch clause can then handle it.
            switch (sceneNumber) {
                case 1 -> {
                    window.setScene(mainScene);
                    window.setTitle("Audio wav with fade-in and fade-out effect"); // Go to second scene
                    sceneText.setText("Browse and select the Audio file to view its waveform, with fade-in");
                    sceneNumber++;
                }
                case 2 -> {
                    refreshNumber = 1;
                    window.setScene(mainScene);
                    window.setTitle("BMP File Viewing");
                    sceneText.setText("Browse and select the image .bmp file to view");
                    sceneNumber++;
                }
                case 3 ->{
                    window.setScene(mainScene);
                    window.setTitle("Audio Waveform without fade-in and fade-out effect");
                    sceneText.setText("Browse and select the Audio file to view its waveform, without fade-in");
                    sceneNumber = 1;
                }

            }
        });

        FileChooser fileChooser = new FileChooser();
        //Window 1 which opens file, displays wave, shows next button
        //Button 2
        fileButton = new Button("Open File...");
        fileButton.setOnAction(e -> {
            File inputFile = fileChooser.showOpenDialog(window);
            if(inputFile != null){
                if(sceneNumber <= 2) {//means we are on the audio part
                    audioChart = new LineChart<>(cbXAxis, cbYAxis);
                    readWAVAndDraw(inputFile);
                    VBox linechartLayout = new VBox();
                    linechartLayout.getChildren().addAll(audioChart, nextButton2, quitButton2);
                    linechartScene = new Scene(linechartLayout);
                    audioChart.getData().add(cbSeries);
                    audioChart.setCreateSymbols(false);
                    if (sceneNumber == 1) {
                        audioChart.setTitle("Audio Waveform - No Fade Effect");
                    } else if (sceneNumber == 2) {
                        audioChart.setTitle("Audio Waveform - With Fade Effect");
                    }
                    window.setScene(linechartScene);
                }
                else{ //means we are on image part (SceneNumber 3)
                    try {
                        //For now just display Image.
                        FileInputStream imageFileStream = new FileInputStream(inputFile);
                        currentImage = new Image(imageFileStream);
                        currentImagePath = inputFile.getAbsolutePath();
                        ImageView imageView = new ImageView(currentImage);
                        imageView.setX(50);
                        imageView.setY(50);
                        imageView.setFitHeight(300);
                        imageView.setFitWidth(300);
                        imageView.setPreserveRatio(true);
                        imageHBox = new HBox(imageView);
                        imageLayout = new VBox(imageHBox);
                        HBox imageButtonLayout = new HBox();
                        imageButtonLayout.getChildren().addAll(refreshButton,nextButton3,quitButton3);
                        imageLayout.getChildren().add(imageButtonLayout);
                        imageScene = new Scene(imageLayout,600,500);
                        window.setScene(imageScene);
                    }
                    catch (FileNotFoundException fileNotFoundException) {
                        fileNotFoundException.printStackTrace();
                    }
                }
            }
        });

        //Audio Layout
        audioLayout = new VBox();
        audioLayout.setAlignment(Pos.CENTER);
        audioLayout.getChildren().addAll(sceneText, fileButton,nextButton,quitButton);
        mainScene = new Scene(audioLayout,400,400);

        //Images!

        //Refresh Button for different Stages of Images
        refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e->{

            switch(refreshNumber){
                case 1 ->{
                    //Currently on normal image display and wishing to go to image and grayscale display.
                    grayScaleImagePath = convertToGrayScale();
                    ImageView imageView = new ImageView(new Image("file:///"+grayScaleImagePath));
                    imageView.setX(50);
                    imageView.setY(50);
                    imageView.setFitHeight(300);
                    imageView.setFitWidth(300);
                    imageView.setPreserveRatio(true);
                    imageHBox.getChildren().add(imageView);
                    refreshNumber++;
                }
                case 2 ->{
                    //Currently on image+grayscale and wishing to go to grayscale+ordered dither

                    //Remove the original image
                    imageHBox.getChildren().remove(0);

                    String ditheredImagePath = ditherImage();
                    ImageView imageView = new ImageView(new Image("file:///"+ditheredImagePath));
                    imageView.setX(50);
                    imageView.setY(50);
                    imageView.setFitHeight(300);
                    imageView.setFitWidth(300);
                    imageView.setPreserveRatio(true);
                    imageHBox.getChildren().add(imageView);
                    refreshNumber++;
                }
                case 3 ->{
                    //Currently on grayscale+dither and wishing to go to original+autolevel

                    //Remove both images
                    imageHBox.getChildren().remove(0);
                    imageHBox.getChildren().remove(0);

                    //Add original image back
                    ImageView imageView = new ImageView(new Image("file:///"+currentImagePath));
                    imageView.setX(50);
                    imageView.setY(50);
                    imageView.setFitHeight(300);
                    imageView.setFitWidth(300);
                    imageView.setPreserveRatio(true);
                    imageHBox.getChildren().add(imageView);

                    //Add autolevel
                    String autolevelImagePath = autolevelImage();
                    ImageView imageView2 = new ImageView(new Image("file:///"+autolevelImagePath));
                    imageView2.setX(50);
                    imageView2.setY(50);
                    imageView2.setFitHeight(300);
                    imageView2.setFitWidth(300);
                    imageView2.setPreserveRatio(true);
                    imageHBox.getChildren().add(imageView2);
                    refreshNumber++;
                }
                case 4 ->{
                    //User is currently on original+autolevel and wishes to go to original image only

                    //Remove both images
                    imageHBox.getChildren().remove(1);




                    refreshNumber = 1;
                }
            }
        });


        window.setScene(mainScene);
        window.setTitle("Audio waveform without fade-in and fade-out effect");
        window.setWidth(950);
        window.show();
    }

    //The user should send in a 2 byte array upon which a short will be returned
    private short convertToShort(byte[] byteArray){
        //Credit to the helpful people on StackOverflow for inspiring  this useful utility function.
        ByteBuffer bb = ByteBuffer.wrap(byteArray);
        bb.order( ByteOrder.LITTLE_ENDIAN);
        return bb.getShort();
    }

    private short fadeEffect(short amplitude,int totalFramesRead, int totalFrames){
        double calcValue;
        int framesFromEnd = totalFrames - totalFramesRead;
        double oneP = 0.01*totalFrames;
        double twoP = 0.02*totalFrames;
        double threeP = 0.03*totalFrames;
        double fourP = 0.04*totalFrames;
        double fiveP = 0.05*totalFrames;
        double sixP = 0.06*totalFrames;
        double sevenP = 0.07*totalFrames;
        double eightP = 0.08*totalFrames;
        double nineP = 0.09*totalFrames;
        double tenP = 0.10*totalFrames;
        double elevenP = 0.11*totalFrames;
        double twelveP = 0.12*totalFrames;
        double thirteenP = 0.13*totalFrames;
        double fourteenP = 0.14*totalFrames;
        double fifteenP = 0.15*totalFrames;
        double sixteenP = 0.16*totalFrames;
        double seventeenP = 0.17*totalFrames;
        double eighteenP = 0.18*totalFrames;
        double nineteenP = 0.19*totalFrames;
        double twentyP = 0.2*totalFrames;



        if(((totalFramesRead >= 1) & (totalFramesRead <= oneP)) | ((framesFromEnd >= 0) & (framesFromEnd <= oneP)) ){
            calcValue = amplitude*0.1;
            return (short) (calcValue + 0.5);
        }
        else if(((totalFramesRead > oneP) & (totalFramesRead <= twoP)) | ((framesFromEnd >= oneP) & (framesFromEnd <= twoP)) ){
            calcValue = amplitude*0.1122018454;
            return (short) (calcValue + 0.5);
        }
        else if(((totalFramesRead > twoP) & (totalFramesRead <= threeP)) | ((framesFromEnd >= twoP) & (framesFromEnd <= threeP)) ){
            calcValue = amplitude*0.1258925412;
            return (short) (calcValue + 0.5);
        }
        else if(((totalFramesRead > threeP) & (totalFramesRead <= fourP)) | ((framesFromEnd >= threeP) & (framesFromEnd <= fourP)) ){
            calcValue = amplitude*0.1412537545;
            return (short) (calcValue + 0.5);
        }
        else if(((totalFramesRead > fourP) & (totalFramesRead <= fiveP)) | ((framesFromEnd >= fourP) & (framesFromEnd <= fiveP)) ){
            calcValue = amplitude*0.1584893192;
            return (short) (calcValue + 0.5);
        }
        else if(((totalFramesRead > fiveP) & (totalFramesRead <= sixP)) | ((framesFromEnd >= fiveP) & (framesFromEnd <= sixP)) ){
            calcValue = amplitude*0.177827941;
            return (short) (calcValue + 0.5);
        }
        else if(((totalFramesRead > sixP) & (totalFramesRead <= sevenP)) | ((framesFromEnd >= sixP) & (framesFromEnd <= sevenP)) ){
            calcValue = amplitude*0.1995262315;
            return (short) (calcValue + 0.5);
        }
        else if(((totalFramesRead > sevenP) & (totalFramesRead <= eightP)) | ((framesFromEnd >= sevenP) & (framesFromEnd <= eightP)) ){
            calcValue = amplitude*0.2238721139;
            return (short) (calcValue + 0.5);
        }
        else if(((totalFramesRead > eightP) & (totalFramesRead <= nineP)) | ((framesFromEnd >= eightP) & (framesFromEnd <= nineP)) ){
            calcValue = amplitude*0.2511886432;
            return (short) (calcValue + 0.5);
        }
        else if(((totalFramesRead > nineP) & (totalFramesRead <= tenP)) | ((framesFromEnd >= nineP) & (framesFromEnd <= tenP)) ){
            calcValue = amplitude*0.2818382931;
            return (short) (calcValue + 0.5);
        }
        else if(((totalFramesRead > tenP) & (totalFramesRead <= elevenP)) | ((framesFromEnd >= tenP) & (framesFromEnd <= elevenP)) ){
            calcValue = amplitude*0.316227766;
            return (short) (calcValue + 0.5);
        }
        else if(((totalFramesRead > elevenP) & (totalFramesRead <= twelveP)) | ((framesFromEnd >= elevenP) & (framesFromEnd <= twelveP)) ){
            calcValue = amplitude*0.3548133892;
            return (short) (calcValue + 0.5);
        }
        else if(((totalFramesRead > twelveP) & (totalFramesRead <= thirteenP)) | ((framesFromEnd >= twelveP) & (framesFromEnd <= thirteenP)) ){
            calcValue = amplitude*0.3981071706;
            return (short) (calcValue + 0.5);
        }
        else if(((totalFramesRead > thirteenP) & (totalFramesRead <= fourteenP)) | ((framesFromEnd >= thirteenP) & (framesFromEnd <= fourteenP)) ){
            calcValue = amplitude*0.4466835922;
            return (short) (calcValue + 0.5);
        }
        else if(((totalFramesRead > fourteenP) & (totalFramesRead <= fifteenP)) | ((framesFromEnd >= fourteenP) & (framesFromEnd <= fifteenP)) ){
            calcValue = amplitude*0.5011872336;
            return (short) (calcValue + 0.5);
        }
        else if(((totalFramesRead > fifteenP) & (totalFramesRead <= sixteenP)) | ((framesFromEnd >= fifteenP) & (framesFromEnd <= sixteenP)) ){
            calcValue = amplitude*0.5623413252;
            return (short) (calcValue + 0.5);
        }
        else if(((totalFramesRead > sixteenP) & (totalFramesRead <= seventeenP)) | ((framesFromEnd >= sixteenP) & (framesFromEnd <= seventeenP)) ){
            calcValue = amplitude*0.6309573445;
            return (short) (calcValue + 0.5);
        }
        else if(((totalFramesRead > seventeenP) & (totalFramesRead <= eighteenP)) | ((framesFromEnd >= seventeenP) & (framesFromEnd <= eighteenP)) ){
            calcValue = amplitude*0.7079457844;
            return (short) (calcValue + 0.5);
        }
        else if(((totalFramesRead > eighteenP) & (totalFramesRead <= nineteenP)) | ((framesFromEnd >= eighteenP) & (framesFromEnd <= nineteenP)) ){
            calcValue = amplitude*0.7943282347;
            return (short) (calcValue + 0.5);
        }
        else if(((totalFramesRead > nineteenP) & (totalFramesRead <= twentyP)) | ((framesFromEnd >= nineteenP) & (framesFromEnd <= twentyP)) ){
            calcValue = amplitude*0.8912509381;
            return (short) (calcValue + 0.5);
        }
        else if((totalFramesRead > twentyP) & (framesFromEnd > twentyP)){
            calcValue = amplitude;
            return (short) (calcValue + 0.5);
        }
        return 0;
    }

    public static void main(String[] args) {
        launch(args);
    }

    private String convertToGrayScale(){
        try {
            File inputImageFile = new File(currentImagePath);
            BufferedImage processImage;
            processImage = ImageIO.read(inputImageFile);
            int width = processImage.getWidth();
            int height = processImage.getHeight();
            for(int i=0; i<height; i++) {
                for(int j=0; j<width; j++) {
                    //gray = (0.2126*Red^2.2) + 0.7152*Green^2.2 + 0.0722*Blue^2.2)1/2.2
                    Color pixelColour = new Color(processImage.getRGB(j, i));
                    int grey =(int) (Math.pow((0.2126*Math.pow(pixelColour.getRed(),2.2)) + (0.7152*Math.pow(pixelColour.getGreen(),2.2) + (0.0722*Math.pow(pixelColour.getBlue(),2.2))),(1/2.2)));
                    Color newColor = new Color(grey,grey,grey);
                    processImage.setRGB(j,i,newColor.getRGB());
                }
            }
            String newFileName = currentImagePath.substring(0,currentImagePath.length()-4);
            newFileName = newFileName+"_grayscale"+".bmp";
            File grayImageFile = new File(newFileName);
            boolean result = grayImageFile.createNewFile();
            ImageIO.write(processImage,"bmp", grayImageFile);
            return newFileName;
        } catch (Exception e) {System.out.println("Can't read file");}//e.printStackTrace();}
        return "";
    }

    private String ditherImage(){
        int n = 4; //Dimension of Dither Matrix
        /*Integer[][] ditherMatrix = new Integer[n][n];
        List<Integer> possibleElements = IntStream.range(0,(n*n)).boxed().collect(Collectors.toList());
        Collections.shuffle(possibleElements);
        int tmpCtr = 0;
        for(int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                ditherMatrix[i][j] = possibleElements.get(tmpCtr);
                tmpCtr++;
            }
        }
        System.out.println(Arrays.deepToString(ditherMatrix));*/
        Integer[][] ditherMatrix = new Integer[][]{{
                    13, 3, 15, 0
            }, {
                    4, 9, 1, 10
            }, {
                    2, 11, 8, 14
            }, {
                    12, 6, 5, 7
            }};
        try {
            File inputImageFile = new File(grayScaleImagePath);
            BufferedImage processImage;
            processImage = ImageIO.read(inputImageFile);
            int width = processImage.getWidth();
            int height = processImage.getHeight();
            Color black = new Color(0,0,0);
            Color white = new Color(255,255,255);
            for(int x = 0; x <width; x++) {
                for(int y = 0; y <height; y++) {
                    int i = x % n;
                    int j = y % n;
                    Color pixelColour = new Color(processImage.getRGB(x,y));
                    int pixel = pixelColour.getGreen();//Idk if the values are equal

                    pixel = (int) (pixel/(256/((n*n)+1)));
                    if(pixel > ditherMatrix[i][j]){
                        processImage.setRGB(x,y,white.getRGB());
                    }else{
                        processImage.setRGB(x,y,black.getRGB());
                    }
                }
            }
            String newFileName = grayScaleImagePath.substring(0,grayScaleImagePath.length()-4);
            newFileName = newFileName+"_dithered"+".bmp";
            File ditherImageFile = new File(newFileName);
            boolean result = ditherImageFile.createNewFile();
            ImageIO.write(processImage,"bmp", ditherImageFile);
            return newFileName;
        } catch (Exception e) {System.out.println("Can't read file");}//e.printStackTrace();}
        return "";
    }

    private String autolevelImage(){
        try {
            File inputImageFile = new File(currentImagePath);
            BufferedImage processImage;
            processImage = ImageIO.read(inputImageFile);
            int width = processImage.getWidth();
            int height = processImage.getHeight();

            //The below arrays store the frequency of the intensity i for each channel. For example, if redIntensities[1] = 3, that means three pixels have red intensity level 1.
            int[] redIntensities = new int[256];
            int[] blueIntensities = new int[256];
            int[] greenIntensities = new int[256];
            //These variables hold the sum of the intensities found for each colour. For example, if the image had five pixels of red intensity 1, and two pixels of red intensity 2, and no other red, sumRed would be 7.
            int sumRed = width*height;
            int sumBlue = width*height;
            int sumGreen = width*height;


            for(int i=0; i<height; i++) {
                for(int j=0; j<width; j++) {
                    //Calculate PMF and CDF
                    Color pixelColour = new Color(processImage.getRGB(j, i));
                    int pixelRed = pixelColour.getRed();
                    int pixelBlue = pixelColour.getBlue();
                    int pixelGreen = pixelColour.getGreen();

                    redIntensities[pixelRed] = redIntensities[pixelRed]+1;
                    blueIntensities[pixelBlue] = blueIntensities[pixelBlue]+1;
                    greenIntensities[pixelGreen] = greenIntensities[pixelGreen]+1;
                }
            }
            //Confirmed that it does indeed add up to 539100.
            //PMF = frequency / sum
            double[] redPMF = new double[256];
            double[] bluePMF = new double[256];
            double[] greenPMF = new double[256];
            for(int i=0;i<redIntensities.length;i++){
                redPMF[i] =(double) (redIntensities[i])/sumRed;
                bluePMF[i] =(double) (blueIntensities[i])/sumBlue;
                greenPMF[i] =(double) (greenIntensities[i])/sumGreen;
            }//Total is almost exactly 1 so we should be fine here


            double[] redCDF = new double[256];
            double[] blueCDF = new double[256];
            double[] greenCDF = new double[256];

            double redTotal = 0;
            double blueTotal = 0;
            double greenTotal = 0;
            for(int i=0;i<redPMF.length;i++){
                redTotal+=redPMF[i];
                blueTotal+=bluePMF[i];
                greenTotal+=greenPMF[i];

                redCDF[i] = redTotal;
                blueCDF[i] = blueTotal;
                greenCDF[i] = greenTotal;
            }
            //The CDF seems to be clear now!

            //PMF and CDF have now been calculated. Next step is to use them by multiplying them by level-1 (255) to normalize
            int[] newRed = new int[256];
            int[] newBlue = new int[256];
            int[] newGreen = new int[256];
            for(int i=0;i<redCDF.length;i++){
                newRed[i] = (int) (redCDF[i] * (255));
                newBlue[i] = (int) (blueCDF[i] * (255));
                newGreen[i] = (int) (greenCDF[i] * (255));
            }

            for(int i=0; i<height; i++) {
                for(int j=0; j<width; j++) {
                    //Get old values

                    Color pixelColour = new Color(processImage.getRGB(j, i));
                    int oldRed = pixelColour.getRed();
                    int oldBlue = pixelColour.getBlue();
                    int oldGreen = pixelColour.getGreen();

                    //Set new values
                    int newRedValue = newRed[oldRed];
                    int newBlueValue = newBlue[oldBlue];
                    int newGreenValue = newGreen[oldGreen];
                    Color newColor = new Color(newRedValue,newGreenValue,newBlueValue);
                    processImage.setRGB(j,i,newColor.getRGB());
                }
            }

            String newFileName = currentImagePath.substring(0,currentImagePath.length()-4);
            newFileName = newFileName+"_autolevel"+".bmp";
            File autolevelImageFile = new File(newFileName);
            boolean result = autolevelImageFile.createNewFile();
            ImageIO.write(processImage,"bmp", autolevelImageFile);
            return newFileName;
        } catch (Exception e) {System.out.println("Can't read file");}//e.printStackTrace();}
        return "";
    }
}
