package demo;

import demo.DbImpl

class GroovyApp {


    final static double KM_TO_DEGREES = 1.0 / 111.196672;
    static double RADIUS;
    static double RADIUS_X = RADIUS;
    static double DISTANCE_X_POINTS;
    static double DISTANCE_Y_POINTS;

    static double MAX_X_START;
    static double MAX_Y_START;

    static double OMEGA_X_MIN;
    static double OMEGA_X_MAX;
    static double OMEGA_Y_MIN;
    static double OMEGA_Y_MAX;

    static double MOD_X_POINT = 0.0;
    static double MOD_Y_POINT = 0.0;

    final static double X_STEP = 0.1;
    final static double Y_STEP = 0.1;

    static boolean CORRECTION;

    long timerLength = 0
    long oneCycleTime = 0
    String country = null

    static List<Polygon> restrictedAreas = new ArrayList<>()

    static void initValues(double radius) {
        CORRECTION = true;
        RADIUS = radius * KM_TO_DEGREES;
        RADIUS_X = RADIUS;
        DISTANCE_X_POINTS = RADIUS * Math.sqrt(3)
        DISTANCE_Y_POINTS = 1.5 * RADIUS

        MAX_X_START = RADIUS * Math.sqrt(3) / 2.0
        MAX_Y_START = RADIUS / 2.0

    }

    static void updateXValues(double y) {
        if (CORRECTION) {
            RADIUS_X = RADIUS / Math.cos(y * (Math.PI / 180.0));
            DISTANCE_X_POINTS = RADIUS_X * Math.sqrt(3)
            MAX_X_START = RADIUS_X * Math.sqrt(3) / 2.0
        }
    }

    static List<Point> defineArea(String country) {
        //Place to implement 'polygon-contains-point' algorithm (defining polygon)
        //https://github.com/sromku/polygon-contains-point

        DbImpl dbImpl = new DbImpl()
        checkRestrictedAreas(country, dbImpl)
        return dbImpl.returnAreaForCountry(country)
    }

    private static void checkRestrictedAreas(String country, DbImpl dbImpl) {
        if (country.equals("Poland")) { //Czy Polska??
            int numberOfRestrictedAreas = Math.toIntExact(dbImpl.getNumberOfRestrictedAreas())
            for (int i = 1; i < numberOfRestrictedAreas + 1; i++) {
                List<Point> points = dbImpl.getRestrictedArea(i)
                List<Point> fixPoints = new ArrayList<Point>();
                for(Point point : points){
                    fixPoints.add(new Point(point.y, point.x));
                }
                List<Point> fixPointsAfterReduce = new ArrayList<>()
                for (int j = 0; j < fixPoints.size(); j++) {
                    if (fixPoints.size() < 1000) {
                        if (j%10 == 0) {
                            fixPointsAfterReduce.add(fixPoints.get(j))
                        }
                    } else if (fixPoints.size() < 10000) {
                        if (j%100 == 0) {
                            fixPointsAfterReduce.add(fixPoints.get(j))
                        }
                    } else {
                        if (j%1000 == 0) {
                            fixPointsAfterReduce.add(fixPoints.get(j))
                        }
                    }
                }
                Polygon polygon = Polygon.BuildPolygon()
                        .addListOfPoints(fixPointsAfterReduce)
                        .build()
                restrictedAreas.add(polygon)
            }
        }
    }

    static void buildOmegaConstrains(List<Point> pointList) {

        double xMaxValue = Double.MIN_VALUE, xMinValue = Double.MAX_VALUE,
               yMinValue = Double.MAX_VALUE, yMaxValue = Double.MIN_VALUE;
        for (Point point : pointList) {
            if (point.x < xMinValue) {
                xMinValue = point.x;
            }
            if (point.x > xMaxValue) {
                xMaxValue = point.x;
            }
            if (point.y > yMaxValue) {
                yMaxValue = point.y;
            }
            if (point.y < yMinValue) {
                yMinValue = point.y;
            }
        }

        OMEGA_X_MIN = xMinValue;
        OMEGA_X_MAX = xMaxValue;
        OMEGA_Y_MIN = yMinValue;
        OMEGA_Y_MAX = yMaxValue;

    }

    List<Point> mainAlgorithm(double Xs, double Ys, Polygon polygon) {
        List<Point> antennasPoints = new ArrayList<>()
        Point checkingPoint = new Point(Xs, Ys)

        double startX = Xs;
        double startY = Ys;

        for (int k = 0; checkingPoint.y < OMEGA_Y_MAX + DISTANCE_Y_POINTS; k++) {
            checkingPoint.y = startY + k * DISTANCE_Y_POINTS;
            updateXValues(checkingPoint.y);
            checkingPoint.x = startX;
            println("New Y. : " + checkingPoint.y)

            for (int i = 0; checkingPoint.x < OMEGA_X_MAX + DISTANCE_X_POINTS; i++) {
                checkingPoint.x = startX + i * DISTANCE_X_POINTS;

                println("X : " + checkingPoint.x + " " + i+1 + " " + "Y: " + checkingPoint.y);

                boolean addPoint;
                if (checkIfPointIsInRestrictedArea(checkingPoint)) {
                    MOD_Y_POINT = 0.0;
                    MOD_X_POINT = 0.0;
                    addPoint = true
                    println("ResArea");
                } else if (checkIfAreaContainsSpecificPoint(checkingPoint, polygon)) {//checking center points
                    MOD_Y_POINT = 0.0;
                    MOD_X_POINT = 0.0;
                    addPoint = true;
                    println("Specific")
                } else if (checkIfAreaContainsExtremePoints(checkingPoint, polygon)) {
                    MOD_Y_POINT = 0.0;
                    MOD_X_POINT = 0.0;
                    addPoint = true;
                    println("Extreme")
                } else {
                    addPoint = false;
                    println("Empty")
                }
                if (addPoint) {
                    Point qualifiedPoint = new Point(checkingPoint.x + MOD_X_POINT, checkingPoint.y + MOD_Y_POINT)
                    antennasPoints.add(qualifiedPoint);
                    println("Added point -  X: " + checkingPoint.x + MOD_X_POINT + " Y: " + checkingPoint.y + MOD_Y_POINT);

                }
                MOD_Y_POINT = 0.0;
                MOD_X_POINT = 0.0;

            }

            if (startX < Xs) {
                startX = Xs;
            } else {
                startX = Xs - (RADIUS_X * Math.sqrt(3) / 2);
            }
        }
        return antennasPoints;
    }

    List<Point> main(double radius, String country) {

        this.country = country
        initValues(radius);
        List<Point> bugList = defineArea(country);
        List<Point> pointList = new ArrayList<Point>();
        for(Point bugPoint : bugList){
            pointList.add(new Point(bugPoint.y, bugPoint.x));
        }
        println("Amount of points: " + pointList.size())
        List<Point> pointListAfterReduce = new ArrayList<>();

        for (int i = 0; i < pointList.size(); i++) {
            if (i%500 == 0) {
                pointListAfterReduce.add(pointList.get(i))
            }
        }

        println("Amount of points: after reduce: " + pointListAfterReduce.size())

        buildOmegaConstrains(pointListAfterReduce);

        println("OMEGA constrains:");
        println("X_MIN: " + OMEGA_X_MIN);
        println("X_MAX: " + OMEGA_X_MAX);
        println("Y_MIN: " + OMEGA_Y_MIN);
        println("Y_MAX: " + OMEGA_Y_MAX);


        Polygon polygon = Polygon.BuildPolygon()
                .addListOfPoints(pointListAfterReduce)
                .build();


        println("Polygon build successful.")


        long no = 0;
        long changeNo = 0;
        List<Point> optimalAntennasPoints = new ArrayList<>();
        boolean init = true;

        for (double y = OMEGA_Y_MIN; y < OMEGA_Y_MIN + MAX_Y_START; y += Y_STEP) {
            updateXValues(y);
            for (double x = OMEGA_X_MIN; x < OMEGA_X_MIN + MAX_X_START; x += X_STEP) {

                List<Point> antennasPoints = mainAlgorithm(x, y, polygon);
                boolean change;
                if (!init) {
                    change = optimalAntennasPoints.size() > antennasPoints.size();
                    //Place to implement additional conditions for off-areas
                    no++;
                    println("NO." + no + " Optimal: " + optimalAntennasPoints.size() + " Current: " + antennasPoints.size() + " changeStatus: NO." + changeNo);
                } else {
                    change = true;
                    init = false;
                }

                if (change) {
                    changeNo = no;
                    optimalAntennasPoints.clear();
                    for (Point antennaPoint : antennasPoints) {
                        Point optimalPoint = new Point(antennaPoint.y, antennaPoint.x);
                        optimalAntennasPoints.add(optimalPoint);
                    }
                }
            }
        }

        println();
        println("Punkty antenowe: ");
        if (optimalAntennasPoints != null) {
            for (Point point : optimalAntennasPoints) {
                println("X: " + point.x + ", " + "Y: " + point.y);
            }
        }


        return optimalAntennasPoints;
    }

    boolean checkIfPointIsInRestrictedArea(Point checkingPoint) {
        if (!country.equals("Poland")) {
            return false
        }
        for (Polygon polygon : restrictedAreas) {
            if (polygon.contains(checkingPoint)) {
                return true
            }
        }
        return false
    }

    static boolean checkIfAreaContainsSpecificPoint(Point point2f, Polygon polygon) {
        //Place to implement 'polygon-contains-point' algorithm (defining polygon)
        //https://github.com/sromku/polygon-contains-point

        return polygon.contains(point2f);

        //Basic polygon (rectangular)
//        if (point2f.x < Area.X_MAX && point2f.x > Area.X_MIN
//                && point2f.y < Area.Y_MAX && point2f.y > Area.Y_MIN) {
//            return true;
//        }
//        return false;
    }

    static boolean checkIfAreaContainsExtremePoints(Point centerPoint, Polygon polygon) {

        println("Halo3")
        if (checkExtremePoint(centerPoint, 0.0, RADIUS, polygon)) {
            MOD_X_POINT = 0.0;
            MOD_Y_POINT = RADIUS;
            return true;
        }
        if (checkExtremePoint(centerPoint, RADIUS_X * Math.sqrt(3) / 2.0, RADIUS / 2.0, polygon)) {
            MOD_X_POINT = RADIUS_X * Math.sqrt(3) / 2.0;
            MOD_Y_POINT = RADIUS / 2.0;
            return true;
        }
        if (checkExtremePoint(centerPoint, RADIUS_X * Math.sqrt(3) / 2.0, (-1) * RADIUS / 2.0, polygon)) {
            MOD_X_POINT = RADIUS_X * Math.sqrt(3) / 2.0;
            MOD_Y_POINT = (-1) * RADIUS / 2.0;
            return true;
        }
        if (checkExtremePoint(centerPoint, 0.0, (-1) * RADIUS, polygon)) {// point No.4
            MOD_X_POINT = 0.0;
            MOD_Y_POINT = (-1) * RADIUS;
            return true
        }
        if (checkExtremePoint(centerPoint, (-1) * RADIUS_X * Math.sqrt(3) / 2.0, (-1) * RADIUS / 2.0, polygon)) {
            MOD_X_POINT = (-1) * RADIUS_X * Math.sqrt(3) / 2.0;
            MOD_Y_POINT = (-1) * RADIUS / 2.0;
            return true
        }
        if (checkExtremePoint(centerPoint, (-1) * RADIUS_X * Math.sqrt(3) / 2.0, RADIUS / 2.0, polygon)) {
            MOD_X_POINT = (-1) * RADIUS_X * Math.sqrt(3) / 2.0;
            MOD_Y_POINT = RADIUS / 2.0;
            return true
        }

        return false
    }


    static boolean checkExtremePoint(Point centerPoint, double xMod, double yMod, Polygon polygon) {


        if (checkIfAreaContainsSpecificPoint(new Point(centerPoint.x + xMod, centerPoint.y + yMod), polygon)) {
            return true;
        }
        return false;

    }

    void calculateTimerParameters(long startTime, long endTiming) {
        double ySize = OMEGA_Y_MIN + MAX_Y_START
        double xSize = OMEGA_X_MIN + MAX_X_START
        double yLength = (ySize - OMEGA_Y_MIN) / Y_STEP
        double xLength = (xSize - OMEGA_X_MIN) / X_STEP
        timerLength = (long) (xLength * yLength)
        oneCycleTime = endTiming - startTime
    }

    long calculateTime(String country, double radius) {

        initValues(radius);
        List<Point> pointList = defineArea(country);
        println("Amount of points: " + pointList.size())
        List<Point> pointListAfterReduce = new ArrayList<>();

        for (int i = 0; i < pointList.size(); i++) {
            if (i%1000 == 0) {
                pointListAfterReduce.add(pointList.get(i))
            }
        }

        println("Amount of points after reduce: " + pointListAfterReduce.size())

        buildOmegaConstrains(pointListAfterReduce);

        println("OMEGA constrains:");
        println("X_MIN: " + OMEGA_X_MIN);
        println("X_MAX: " + OMEGA_X_MAX);
        println("Y_MIN: " + OMEGA_Y_MIN);
        println("Y_MAX: " + OMEGA_Y_MAX);

        Polygon polygon = Polygon.BuildPolygon()
                .addListOfPoints(pointListAfterReduce)
                .build();


        println("Polygon build successful.")

        long no = 0;
        long changeNo = 0;
        List<Point> optimalAntennasPoints = new ArrayList<>();
        boolean init = true;

        for (double y = OMEGA_Y_MIN; y < OMEGA_Y_MIN + MAX_Y_START; y += Y_STEP) {
            updateXValues(y);
            for (double x = OMEGA_X_MIN; x < OMEGA_X_MIN + MAX_X_START; x += X_STEP) {

                long startTime = System.currentTimeMillis()

                List<Point> antennasPoints = mainAlgorithm(x, y, polygon);
                boolean change;
                if (!init) {
                    change = optimalAntennasPoints.size() > antennasPoints.size();
                    //Place to implement additional conditions for off-areas
                    no++;
                    println("NO." + no + " Optimal: " + optimalAntennasPoints.size() + " Current: " + antennasPoints.size() + " changeStatus: NO." + changeNo);
                    println(" ");
                    println(" ");
                    println("-");
                    println("-");
                    println("-");
                    println("-");
                } else {
                    change = true;
                    init = false;
                }

                if (change) {
                    changeNo = no;
                    optimalAntennasPoints.clear();
                    for (Point antennaPoint : antennasPoints) {
                        Point optimalPoint = new Point(antennaPoint.y, antennaPoint.x)
                        optimalAntennasPoints.add(optimalPoint);
                    }
                }

                long endTime = System.currentTimeMillis()
                calculateTimerParameters(startTime, endTime)

                break
            }
            break
        }
        return timerLength * oneCycleTime
    }

}




