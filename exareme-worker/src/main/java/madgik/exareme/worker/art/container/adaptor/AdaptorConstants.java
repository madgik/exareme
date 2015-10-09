/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.adaptor;

import madgik.exareme.utils.properties.AdpProperties;

/**
 * @author heraldkllapi
 */
public class AdaptorConstants {

    public static AdaptorImplType adaptorImpl = AdaptorImplType
        .valueOf(AdpProperties.getArtProps().getString("art.container.adaptor.impl"));
}
