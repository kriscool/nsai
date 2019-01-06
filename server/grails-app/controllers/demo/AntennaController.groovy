package demo


import grails.rest.*
import grails.converters.*

class AntennaController {

    GroovyApp groovyApp;

    static responseFormats = ['json', 'xml']

    def index(String country, double radius) {
        if (request.method == 'GET') {
            groovyApp = new GroovyApp();
		println(country)
            	List<Point> list = groovyApp.main(radius,country)           
		String points = ""

            for (Point point : list) {
                points += "[" + point.x + ", " + point.y + ", " + params.radius + "], ";
            }
            points = points.substring(0, points.length() - 2);
            render '{"data":{"radius":' + params.radius + ', "country":"' + params.country + '", "points":[' + points + ']}}';
        }
    }
}
