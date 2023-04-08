ARG from_image
FROM $from_image

ARG from_image
ARG project_name
ARG artifact
ARG artifact_name
ARG artifact_version
ARG vcs_url
ARG vcs_ref

LABEL se.inera.from_image=${from_image}       \
      se.inera.project=${project_name}        \
      se.inera.artifact=${artifact}           \
      se.inera.artifact_name=${artifact_name} \
      se.inera.version=${artifact_version}    \
      se.inera.vcs_url=${vcs_url}             \
      se.inera.vcs_ref=${vcs_ref}

ENV APP_NAME=$artifact
ENV SCRIPT_DEBUG=true

ADD /web/build/libs/*.war $JWS_HOME/webapps/ROOT.war
