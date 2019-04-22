/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.worker.arm.compute.cluster;

import madgik.exareme.common.art.entity.EntityName;

import java.io.Serializable;

/**
 * @author Christos
 */
public class PatternElement implements Serializable {

    public EntityName machine = null;
    public int relative_name = -1;
    public double duration;
    public double relative_start_time;


    public void setParameters(int relative_name, double duration, double relative_start_time) {

        this.relative_name = relative_name;
        this.duration = duration;
        this.relative_start_time = relative_start_time;
    }

    public void setParameters(EntityName machine, double duration, double relative_start_time) {

        this.machine = machine;
        this.duration = duration;
        this.relative_start_time = relative_start_time;
    }

    public void initiate() {
        machine = null;
        relative_name = -1;
    }

}

