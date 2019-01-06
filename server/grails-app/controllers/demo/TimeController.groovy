package demo


import grails.rest.*
import grails.converters.*

class TimeController {

	    GroovyApp groovyApp;

	static responseFormats = ['json', 'xml']
	
    def index(String country, double radius) {
        if (request.method == 'GET') {
            groovyApp = new GroovyApp();
		long time = groovyApp.calculateTime(country, radius);
            render '{"data":{"radius":' + radius + ', "country":"' + country + '", "time":' + time + '}}';
        }
    }
}
