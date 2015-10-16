/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package madgik.exareme.worker.arm.storage.client.utils;

/**
 * Assumptions :
 * + thread-safe
 *
 * @author alexpap
 */
public class ArmStorageClientUtils {

    /**
     * @param nbytes
     * @return
     * @see default 'io.bytes.per.checksum' property
     */
    public static long roundFileLength(long nbytes) {
        if (nbytes <= 0)
            return 0;
        else if (nbytes <= 512)
            return nbytes;
        else if (nbytes % 512 == 0)
            return nbytes;
        else
            return (int) (nbytes / 512 + 1) * 512;
    }
}
