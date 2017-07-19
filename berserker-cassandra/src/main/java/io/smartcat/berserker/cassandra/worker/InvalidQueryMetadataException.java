package io.smartcat.berserker.cassandra.worker;

/**
 * Indicates invalid query metadata.
 */
public class InvalidQueryMetadataException extends RuntimeException {

    private static final long serialVersionUID = -5024863994836325282L;

    /**
     * Constructs an InvalidQueryMetadataException with no detail message. A detail message is a String that describes
     * this particular exception.
     */
    public InvalidQueryMetadataException() {
    }

    /**
     * Constructs an InvalidQueryMetadataException with the specified detail message. A detail message is a String that
     * describes this particular exception.
     *
     * @param message The string that contains a detailed message.
     */
    public InvalidQueryMetadataException(String message) {
        super(message);
    }
}
