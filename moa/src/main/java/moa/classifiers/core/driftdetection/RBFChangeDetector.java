package moa.classifiers.core.driftdetection;

import com.github.javacliparser.FloatOption;
import com.github.javacliparser.IntOption;
import moa.core.ObjectRepository;
import moa.tasks.TaskMonitor;

import java.util.*;

public class RBFChangeDetector extends AbstractChangeDetector {
    private static final long serialVersionUID = 5210470661274384763L;

    public FloatOption sigma = new FloatOption(
            "sigma",
            's',
            "Gaussian radius limiter",
            2.0, Float.MIN_NORMAL, Float.MAX_VALUE);

    public FloatOption lambda = new FloatOption(
            "lambda",
            'e',
            "Minimun threshold",
            0.5, 0.1, Float.MAX_VALUE);

    // Params
    Double actualCenter = null;
    ArrayList<Double> centers = new ArrayList<>();

    Integer timeInstant = 0;
    ArrayList<Integer> timeInstants = new ArrayList<>();

    public RBFChangeDetector() {
        resetLearning();
    }

    @Override
    public void resetLearning() {
        this.isChangeDetected = false;
    }

    @Override
    public void input(double inputData) {
        if (this.isChangeDetected == true || this.isInitialized == false) {
            resetLearning();
            this.isInitialized = true;
        }

        timeInstant++;

        Double activation = 0.0D;
        Double activationLambda = this.lambda.getValue();
        Double distance = 0.0D;
        Double activatedCenter = null;

        for (Double center : centers) {
            distance = Math.sqrt(Math.pow(inputData - center, 2.0));
            // gaussian
            activation = Math.exp(-Math.pow(this.sigma.getValue() * distance, 2));
            // activation = Math.exp(-((Math.pow(distance, 2.0)) / (2.0 * Math.pow(this.sigma.getValue(), 2.0))));

            if (activation >= activationLambda) {
                activatedCenter = center;
                activationLambda = activation;
            }
        }

        this.estimation = activationLambda;
        this.isChangeDetected = false;
        this.isWarningZone = false;
        this.delay = 0;

        if (activatedCenter == null) {
            centers.add(inputData);
            activatedCenter = inputData;
        }

        if (! activatedCenter.equals(actualCenter)) {
            if (actualCenter == null) {
                actualCenter = activatedCenter;
            } else {
                System.out.println("drift: timeInstant - " + timeInstant + " | input:" + inputData + " | centers: " + centers + " | actualCenter - " + actualCenter + " | activatedCenter - " + activatedCenter + " | distance: " + distance + " | "  + "activation:" + activation);
                actualCenter = activatedCenter;
                this.isChangeDetected = true;
            }
        }
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
