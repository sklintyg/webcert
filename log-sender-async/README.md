För att köra log-sender lokalt, starta med:

mvn jetty:run -Pjetty

Konfigurera lokalt

För att koppla ihop lokal webcert med lokal logsender, gör följande:

1. Starta en riktig ActiveMQ lokalt.
2. Öppna jetty-web.xml och ändra så jms/ConnectionFactory går över TCP och port 61617:


     <New id="ConnectionFactory" class="org.eclipse.jetty.plus.jndi.Resource">
        <Arg>
          <Ref refid="webAppContext" />
        </Arg>
        <Arg>jms/ConnectionFactory</Arg>
        <Arg>
          <New class="org.apache.activemq.ActiveMQConnectionFactory">
              <!-- <Arg>vm://localhost?broker.persistent=false</Arg> -->
              <Arg>tcp://localhost:61617</Arg>
          </New>
        </Arg>
      </New>
      
3. Bygg om och starta webcert.
4. Starta log-sender, kom ihåg -Pjetty för log-sender.

Nu kommer log-sender plocka upp lokala loggmeddelanden postade av Webcert.

På en mer övergripande nivå, kolla i LogSender.java och dess inre klass:

        private final class JmsToLogSender implements SessionCallback<Boolean> {
        
Först sker en kontroll om det finns något att skicka. Om så är fallet 