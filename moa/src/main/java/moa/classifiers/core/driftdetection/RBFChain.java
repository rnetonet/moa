package moa.classifiers.core.driftdetection;

import com.github.javacliparser.FloatOption;
import com.github.javacliparser.IntOption;
import moa.core.ObjectRepository;
import moa.tasks.TaskMonitor;

import java.text.DecimalFormat;
import java.util.*;

public class RBFChain extends AbstractChangeDetector {
    private static final long serialVersionUID = 5210470661274384763L;

    public FloatOption sigma = new FloatOption(
            "sigma",
            's',
            "Delimits the Gaussian radius",
            2.0, Float.MIN_NORMAL, Float.MAX_VALUE);

    public FloatOption lambda = new FloatOption(
            "lambda",
            'e',
            "Minimum threshold to activate a center",
            0.5, 0.1, Float.MAX_VALUE);

    public FloatOption alpha = new FloatOption(
            "alpha",
            'e',
            "Probability increase factor in Markov Chain",
            0.05, 0.1, Float.MAX_VALUE);

    public FloatOption delta = new FloatOption(
            "delta",
            'e',
            "Minimum threshold to consider probability in the Markov Chain as a Concept Drift indication.",
            1.0, 0.1, Float.MAX_VALUE);

    // Params
    ArrayList<Double> groups = new ArrayList<>();
    Double activatedGroup = null;
    Double conceptGroup = null;
    HashMap<String, Double> markov = new HashMap<>();

    public RBFChain() {
        resetLearning();
    }

    private static DecimalFormat df = new DecimalFormat("#.##");

    @Override
    public void resetLearning() {
        this.isChangeDetected = false;
    }

    public Double updateMarkov(Double conceptGroup, Double activatedGroup) {
        String origin = df.format(conceptGroup);
        String destination = df.format(activatedGroup);
        String separator = "|";
        String transition = origin + separator + destination;

        if (! markov.containsKey(transition)) {
            markov.put(transition, 0.0);
        }

        // Increment
        markov.put(transition, markov.get(transition) + this.alpha.getValue());

        // Decrement
        int otherTransitions = 0;
        for (String key : markov.keySet()) {
            if (key.startsWith(origin + separator) & ! key.equals(transition)) {
                otherTransitions++;
            }
        }

        if (otherTransitions > 0) {
            Double reductionFactor = this.alpha.getValue() / otherTransitions;
            for (String key : markov.keySet()) {
                if (key.startsWith(origin + separator) & ! key.equals(transition)) {
                    markov.put(key, markov.get(key) - reductionFactor);
                }
            }
        }

        // Adjust
        for (String key : markov.keySet()) {
            if (key.startsWith(origin + separator)) {
                if (markov.get(key) > 1) {
                    markov.put(key, 1.0);
                } else if (markov.get(key) < 0) {
                    markov.put(key, 0.0);
                }

            }
        }

        return markov.get(transition);
    }

    @Override
    public void input(double event) {
        if (this.isChangeDetected == true || this.isInitialized == false) {
            resetLearning();
            this.isInitialized = true;
        }

        Double _lambda = this.lambda.getValue();
        for (Double group : groups) {
            Double distance = Math.sqrt(Math.pow(event - group, 2.0));
            // gaussian
            Double activation = Math.exp(-Math.pow(this.sigma.getValue() * distance, 2));
            // activation = Math.exp(-((Math.pow(distance, 2.0)) / (2.0 * Math.pow(this.sigma.getValue(), 2.0))));

            if (activation >= _lambda) {
                activatedGroup = group;
                _lambda = activation;
            }
        }

        if (activatedGroup == null) {
            groups.add(event);
            activatedGroup = event;
        }
        if (conceptGroup == null) {
            conceptGroup = activatedGroup;
        }

        Double probability = updateMarkov(conceptGroup, activatedGroup);

        if (conceptGroup != activatedGroup) {
            if (probability < this.delta.getValue()) {
                this.isWarningZone = true;
            } else {
                this.isChangeDetected = true;
                this.isWarningZone = false;
                conceptGroup = activatedGroup;
            }
        }

//        this.estimation = activationLambda;
//        this.isChangeDetected = false;
//        this.isWarningZone = false;
//        this.delay = 0;
//
//        if (activatedCenter == null) {
//            centers.add(event);
//            activatedCenter = event;
//        }
//
//        if (! activatedCenter.equals(actualCenter)) {
//            if (actualCenter == null) {
//                actualCenter = activatedCenter;
//            } else {
//                System.out.println("drift: timeInstant - " + timeInstant + " | event:" + event + " | centers: " + centers + " | actualCenter - " + actualCenter + " | activatedCenter - " + activatedCenter + " | distance: " + distance + " | "  + "activation:" + activation);
//                actualCenter = activatedCenter;
//                this.isChangeDetected = true;
//            }
//        }
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
