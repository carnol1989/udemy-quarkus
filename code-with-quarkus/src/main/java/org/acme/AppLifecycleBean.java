package org.acme;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

@ApplicationScoped
public class AppLifecycleBean {

  private static final Logger LOGGER = Logger.getLogger("ListenerBean");

  void onStart(@Observes StartupEvent evt){
    LOGGER.info("The aplication is starting...");
  }

  void onStop(@Observes ShutdownEvent evt){
    LOGGER.info("The aplication is stopping...");
  }

}
