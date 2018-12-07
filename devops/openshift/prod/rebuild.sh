#!/bin/bash
proj=`basename $(pwd)`
check=`/usr/bin/oc project | awk '{print $3 " - " $6}'`
echo "Vill du göra ändringen av $proj på $check ? y/n"
read answer
if [[ "$answer" == "y" ]]; then echo ok; else echo quitting; exit; fi
oc delete configmap $proj-config;  oc create configmap $proj-config --from-file=config/
oc delete configmap $proj-configmap-envvar; oc create -f configmap-vars.yaml
oc delete secret $proj-secret-envvar; oc create -f secret-vars.yaml
oc delete secret $proj-env; oc create secret generic $proj-env --from-file=env/ --type=Opaque
oc delete secret $proj-certifikat; oc create secret generic $proj-certifikat --from-file=certifikat/ --type=Opaque
oc rollout cancel dc/$proj
echo Sleep 10...
sleep 10
oc rollout latest $proj
echo Sleep 10...
sleep 15
oc logs -f `oc get po | grep $proj | sort -t'-' -n -k2 | tail -n 2 |grep -v deploy | awk '{print $1}'`
