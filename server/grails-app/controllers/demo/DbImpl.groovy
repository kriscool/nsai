package demo

import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement
import java.sql.Connection
import org.postgresql.PGConnection
import org.postgis.MultiPolygon
import org.postgis.PGgeometry
import org.postgis.Point

class DbImpl {

    Connection conn;

    void init() throws Exception{
        Class.forName("org.postgresql.Driver");
        String url = "jdbc:postgresql://localhost:5432/poland";
        conn = DriverManager.getConnection(url, "postgres", "postgres");
        ((org.postgresql.PGConnection)conn).addDataType("geometry",Class.forName("org.postgis.PGgeometry"));
        ((org.postgresql.PGConnection)conn).addDataType("box3d",Class.forName("org.postgis.PGbox3d"));
    }

    List<demo.Point> returnAreaForCountry(String country) {
        init()
        List<demo.Point> pointsForCountry = new ArrayList<>()
        String sqlQuery="";
        switch(country){
            case "Poland":sqlQuery= "select geom from poland_border";
                break;
            case "Germany":sqlQuery= "select geom from germany_border";
                break;
            case "France":sqlQuery= "select geom from france_border";
                break;
            case "Czech Republic":sqlQuery= "select geom from czech_border";
                break;
        }

        try {


            Statement s = conn.createStatement();
            ResultSet r = s.executeQuery(sqlQuery);
            while (r.next()) {
                PGgeometry geom = (PGgeometry) r.getObject(1);
                MultiPolygon pl = (MultiPolygon) geom.getGeometry();

                for (int i = 0; i < pl.numPoints(); i++) {
                    Point pointsql = pl.getPoint(i);
                    demo.Point p = new demo.Point(pointsql.getY(),pointsql.getX())
                    pointsForCountry.add(p)
                }
            }
            s.close();

            return pointsForCountry;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    Long getNumberOfRestrictedAreas() throws Exception{
        init()
        Long size = null;
        Statement s = conn.createStatement();
        ResultSet r = s.executeQuery("select count(id) from parki_narodowe");
        while( r.next() ) {
            size =(Long) r.getObject(1);
        }
        s.close();
        return size;
    }

    List<demo.Point> getRestrictedArea(int id) throws Exception{
        init()
        List<demo.Point> listOfPoints = new ArrayList<>();
        String sql = "SELECT ST_TRANSFORM(parki_narodowe.geom,4326) AS LongLat FROM parki_narodowe where id =";
        sql+=id;
        Statement s = conn.createStatement();
        ResultSet r = s.executeQuery(sql);
        while( r.next() ) {

            PGgeometry geom = (PGgeometry)r.getObject(1);
            geom.getGeoType();
            MultiPolygon pl = (MultiPolygon)geom.getGeometry();
            for( int i = 0; i < pl.numPoints(); i++) {
                Point pointsql = pl.getPoint(i);
                demo.Point p = new demo.Point(pointsql.getY(),pointsql.getX())
                listOfPoints.add(p)

            }
        }
        s.close();
        return listOfPoints;
    }
}
