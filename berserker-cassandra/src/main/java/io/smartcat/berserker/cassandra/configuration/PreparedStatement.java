package io.smartcat.berserker.cassandra.configuration;

/**
 * Prepared statement with <code>id</code> and <code>query</code>.
 */
public class PreparedStatement {

    private String id;
    private String query;

    /**
     * Constructs empty prepared statement.
     */
    public PreparedStatement() {
    }

    /**
     * Constructs prepared statement with specified <code>id</code> and <code>query</code>.
     *
     * @param id Statement id.
     * @param query Statement query.
     */
    public PreparedStatement(String id, String query) {
        this.id = id;
        this.query = query;
    }

    /**
     * Returns statement's id.
     *
     * @return Statement's id.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets statement's id.
     *
     * @param id Id to set.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns statement's query.
     *
     * @return Statement's query.
     */
    public String getQuery() {
        return query;
    }

    /**
     * Sets statement's query.
     *
     * @param query Query to set.
     */
    public void setQuery(String query) {
        this.query = query;
    }
}
