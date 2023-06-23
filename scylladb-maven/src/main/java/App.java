import com.datastax.driver.core.*;

public class App {

        //static Cluster cluster = Cluster.builder().addContactPoints("scylla-node1", "scylla-node2", "scylla-node3").build();
        static Cluster cluster = Cluster.builder().addContactPoints("10.61.60.108").build();
        static Session session = cluster.connect("catalog");
        static PreparedStatement insert = session.prepare("INSERT INTO mutant_data (first_name,last_name,address,picture_location) VALUES (?,?,?,?)");
        static PreparedStatement delete = session.prepare("DELETE FROM mutant_data WHERE first_name = ? and last_name = ?");


        public static void selectQuery() {
                System.out.print("\n\nDisplaying Results:");
                ResultSet results = session.execute("SELECT * FROM catalog.mutant_data");
                for (Row row : results) {
                        String first_name = row.getString("first_name");
                        String last_name = row.getString("last_name");
                        System.out.print("\n" + first_name + " " + last_name);
                }
        }

        public static void insertQuery(String first_name, String last_name, String address, String picture_location) {
                System.out.print("\n\nInserting Mike Tyson......");
                session.execute(insert.bind(first_name,last_name,address,picture_location));
                selectQuery();
        }

        public static void deleteQuery(String first_name, String last_name) {
                System.out.print("\n\nDeleting Mike Tyson......");
                session.execute(delete.bind(first_name,last_name));
                selectQuery();
        }

        public static void main(String[] args) {
                selectQuery();
                insertQuery("Mike", "Tyson", "12345 Foo Lane", "http://www.facebook.com/mtyson");
                insertQuery("Alex", "Jones", "56789 Hickory St", "http://www.facebook.com/ajones");
                deleteQuery("Mike", "Tyson");
                deleteQuery("Alex", "Jones");
                cluster.close();
        }

}
