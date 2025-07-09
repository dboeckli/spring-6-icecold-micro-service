TODO: FILL ME



### Deployment with Helm

Be aware that we are using a different namespace here (not default).

To run maven filtering for destination target/helm
```bash
mvn clean install -DskipTests 
```

Go to the directory where the tgz file has been created after 'mvn install'
```powershell
cd target/helm/repo
```

unpack
```powershell
$file = Get-ChildItem -Filter spring-6-icecold-micro-service-v*.tgz | Select-Object -First 1
tar -xvf $file.Name
```

install
```powershell
$APPLICATION_NAME = Get-ChildItem -Directory | Where-Object { $_.LastWriteTime -ge $file.LastWriteTime } | Select-Object -ExpandProperty Name
helm upgrade --install $APPLICATION_NAME ./$APPLICATION_NAME --namespace spring-6-icecold-micro-service --create-namespace --wait --timeout 8m --debug --render-subchart-notes
```

show logs
```powershell
kubectl get pods -l app.kubernetes.io/name=$APPLICATION_NAME -n spring-6-icecold-micro-service
```
replace $POD with pods from the command above
```powershell
kubectl logs $POD -n spring-6-icecold-micro-service --all-containers
```

test
```powershell
helm test $APPLICATION_NAME --namespace spring-6-icecold-micro-service --logs
```

uninstall
```powershell
helm uninstall $APPLICATION_NAME --namespace spring-6-icecold-micro-service
```

delete all
```powershell
kubectl delete all --all -n spring-6-icecold-micro-service
```

create busybox sidecar
```powershell
kubectl run busybox-test --rm -it --image=busybox:1.36 --namespace=spring-6-icecold-micro-service --command -- sh
```

and analyze kafka connections
```powershell
nslookup spring-6-icecold-micro-service-kafka.spring-6-icecold-micro-service.svc.cluster.local

nc -zv spring-6-icecold-micro-service-kafka.spring-6-icecold-micro-service.svc.cluster.local 29092
echo "Exit code for port 29092: $?"
```

You can use the actuator rest call to verify via port 30080