/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.serialization;

import java.io.*;

public class SerializedObject implements Serializable {

    private static final long serialVersionUID = 1L;
    private byte[] m_storedObjectArray;

    public SerializedObject(Serializable toStore) throws Exception {
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        ObjectOutputStream p = new ObjectOutputStream(new BufferedOutputStream(ostream));

        p.writeObject(toStore);
        p.flush();
        p.close(); // used to be ostream.close() !
        m_storedObjectArray = ostream.toByteArray();
    }

    public SerializedObject(byte[] bytes) throws Exception {
        m_storedObjectArray = bytes;
    }

    public Serializable getObject() throws Exception {
        ByteArrayInputStream istream = new ByteArrayInputStream(m_storedObjectArray);
        ObjectInputStream p;
        p = new ObjectInputStream(new BufferedInputStream(istream));
        Serializable toReturn = (Serializable) p.readObject();
        istream.close();
        return toReturn;
    }

    public byte[] getBytes() {
        return m_storedObjectArray;
    }
}
