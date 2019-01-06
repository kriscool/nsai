package demo

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Specification
import org.hibernate.SessionFactory

@Integration
@Rollback
class CoverageServiceSpec extends Specification {

    CoverageService coverageService
    SessionFactory sessionFactory

    private Long setupData() {
        // TODO: Populate valid domain instances and return a valid ID
        //new Coverage(...).save(flush: true, failOnError: true)
        //new Coverage(...).save(flush: true, failOnError: true)
        //Coverage coverage = new Coverage(...).save(flush: true, failOnError: true)
        //new Coverage(...).save(flush: true, failOnError: true)
        //new Coverage(...).save(flush: true, failOnError: true)
        assert false, "TODO: Provide a setupData() implementation for this generated test suite"
        //coverage.id
    }

    void "test get"() {
        setupData()

        expect:
        coverageService.get(1) != null
    }

    void "test list"() {
        setupData()

        when:
        List<Coverage> coverageList = coverageService.list(max: 2, offset: 2)

        then:
        coverageList.size() == 2
        assert false, "TODO: Verify the correct instances are returned"
    }

    void "test count"() {
        setupData()

        expect:
        coverageService.count() == 5
    }

    void "test delete"() {
        Long coverageId = setupData()

        expect:
        coverageService.count() == 5

        when:
        coverageService.delete(coverageId)
        sessionFactory.currentSession.flush()

        then:
        coverageService.count() == 4
    }

    void "test save"() {
        when:
        assert false, "TODO: Provide a valid instance to save"
        Coverage coverage = new Coverage()
        coverageService.save(coverage)

        then:
        coverage.id != null
    }
}
