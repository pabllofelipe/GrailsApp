package hubbub

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.*
import spock.lang.Specification

@Integration
@Rollback
class UserIntegrationSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    def "Saving our first user to the database"() {
        given: "A brand new user"
        def joe = new User(loginId: 'joe', password: 'secret',
                homepage: 'http://www.grailsinaction.com')

        when: "the user is saved"
        joe.save()

        then: "it saved successfully and can be found in the database"
        joe.errors.errorCount == 0
        joe.id != null
        User.get(joe.id).loginId == joe.loginId
    }

    def "Updating a saved user before"(){
        given: "A existing user"
        def existingUser = new User(loginId: 'joe', password: 'secret',
                homepage: 'http://www.grailsinaction.com')
        existingUser.save(failOnError: true)

        when: "A property is changed"
        def FoundUser = User.get(existingUser.id)
        FoundUser.password = "changed"
        FoundUser.save(failOnError: true)

        then: "the change is reflected to database"
        User.get(existingUser.id).password == 'changed'
    }

    def "Deleting a User"(){
        given: "An existing user"
        def existingUser = new User(loginId: 'joe', password: 'secret',
                homepage: 'http://www.grailsinaction.com')
        existingUser.save(failOnError:true)

        when: "The user is deleted"
        def FoundUser = User.get(existingUser.id)
        FoundUser.delete(flush: true)

        then: "the user was deleted from database"
        !User.exists(FoundUser.id)
    }

    def "Saving an user with invalid properties causes an error"(){
        given: "An new user"
        def user = new User(loginId: "joe", password: 'tiny',
                            homepage: 'not-a-url')

        when: "the user is validated"
        user.validate()

        then:
        user.hasErrors()
        !user.errors.getFieldError("loginId")
    }

    def "recovering from a failing save in database due invalid properties"(){
        given: "An new user that has invalid properties"
        def user = new User(loginId: "joe", password: 'tiny',
                homepage: 'not-a-url')
        assert user.save() == null
        assert user.hasErrors()

        when: "We fix invalid properties"
        user.password = "tiny123"
        user.homepage = "http://www.grailsinaction.com"
        user.validate()

        then: "the user saves and validate is fine"
        !user.hasErrors()
        user.save()

    }
}
