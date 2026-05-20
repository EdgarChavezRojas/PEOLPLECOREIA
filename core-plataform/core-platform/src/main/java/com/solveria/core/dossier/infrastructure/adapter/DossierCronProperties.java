package com.solveria.core.dossier.infrastructure.adapter;

// esto se tiene que definir en los proximos dias, configurarlo desde nuestro
// importante tambien, implementar el @EnableScheduling despues del cron para que funcione el
// proceso batch
// definir a futuro, cuales son los eventos que va a escuchar para aplicar los casos de uso
// y tambien cuales casos de uso van a un controler
public final class DossierCronProperties {

  // public static final String DOCUMENT_EXPIRATION_CRON = "${dossier.cron.document-expiration}";

  private DossierCronProperties() {}
}
