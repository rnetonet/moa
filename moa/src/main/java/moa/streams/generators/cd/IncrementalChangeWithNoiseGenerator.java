/*
 *    AbruptChangeGenerator.java
 *    Copyright (C) 2012 University of Waikato, Hamilton, New Zealand
 *    @author Albert Bifet (abifet@cs.waikato.ac.nz)
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package moa.streams.generators.cd;

import com.github.javacliparser.FloatOption;

import java.util.Random;

public class IncrementalChangeWithNoiseGenerator extends AbstractConceptDriftGenerator {

    public FloatOption changeDriftOption = new FloatOption("changeDrift", 'c',
            "The magnitude of change.", .001, 0.0, Double.MAX_VALUE);

    public FloatOption minNoiseOption = new FloatOption("minNoiseOption", 'x',
            "The min value of noise to be added", 0.0, -2.0, 0.0);

    public FloatOption maxNoiseOption = new FloatOption("maxNoiseOption", 'y',
            "The max value of noise to be added", 0.0, 0.0, 2.0);

    @Override
    protected double nextValue() {
        double res;
        double t = this.numInstances;
        this.change = (t == this.period ) ? true : false;
        res = (t < this.period ) ? .2 : .2 + (t - this.period) * changeDriftOption.getValue();
        res = res > 1.0 ? 1.0 : res;

        double random = 0.0;
        do {
            Random r = new Random();
            random = this.minNoiseOption.getValue() + r.nextDouble() * (this.maxNoiseOption.getValue() - this.minNoiseOption.getValue());
        } while (res + random < 0 || res + random > 1);


        return res + random;
    }
}
