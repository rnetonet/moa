package moa.classifiers.core.driftdetection;

import com.github.javacliparser.FloatOption;
import com.github.javacliparser.IntOption;
import moa.core.ObjectRepository;
import moa.tasks.TaskMonitor;

import java.util.*;

public class RBFChangeDetector extends AbstractChangeDetector {
    private static final long serialVersionUID = 5210470661274384763L;

    // Params
    // Double alpha = 0.001; // Used for center movimentation
    Double sigma = 150.0;
    Double threshold = 0.9;

    Double actualCenter = null;
    ArrayList<Double> centers = new ArrayList<>();

    Integer timeInstant = 0;
    ArrayList<Integer> timeInstants = new ArrayList<>();

    public RBFChangeDetector() {
        resetLearning();
    }

    @Override
    public void resetLearning() {
        // this.isChangeDetected = false;
        // this.isInitialized = false;
    }

    @Override
    public void input(double inputData) {
        if (this.isChangeDetected) {
            this.isChangeDetected = false;
            return;
        }

        Double activation = 0.0D;
        Double activationMax = this.threshold;
        Double distance = 0.0D;
        Double activatedCenter = null;

        for (Double center : centers) {
            distance = Math.sqrt(Math.pow(inputData - center, 2.0));
            activation = Math.exp(-(Math.pow(distance, 2.0)) / (2.0 * Math.pow(this.sigma, 2.0))); //sigmoid function

            System.out.println("inputData = " + inputData + ", " + "distance = " + distance + ", " + "activation = " + activation);

            if (activation > activationMax) {
                activatedCenter = center;
                activationMax = activation;
            }
        }

        // assume activation == activatedCenter
        if (activatedCenter == null) {
            centers.add(inputData);
            actualCenter = inputData;
            activatedCenter = inputData;
        }

        if (! actualCenter.equals(activatedCenter)) {
            actualCenter = activatedCenter;
            this.isChangeDetected = true;
        }

        timeInstant++;
    }

    @Override
    public void getDescription(StringBuilder sb, int indent) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void prepareForUseImpl(TaskMonitor monitor,
                                     ObjectRepository repository) {
        // TODO Auto-generated method stub
    }
}
