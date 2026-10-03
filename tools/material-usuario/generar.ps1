param([switch]$SoloManual,[switch]$SoloVideo,[string]$JdkHome='C:/Program Files/Java/jdk-17')
$ErrorActionPreference='Stop'
$raiz=[IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
Set-Location -LiteralPath $raiz
$paquete=Get-ChildItem -LiteralPath dist -Directory -Filter 'PuntoDeVenta-*' | Sort-Object Name -Descending | Select-Object -First 1
if(-not $paquete){throw 'Generá primero el paquete del programa.'}
$cp=(Join-Path $paquete.FullName 'PuntoDeVenta.jar')+';'+((Get-ChildItem -LiteralPath build/material-tools -Filter '*.jar' | ForEach-Object FullName) -join ';')
& (Join-Path $JdkHome 'bin/javac.exe') -encoding UTF-8 --release 8 -cp $cp -d build/material-tools/classes src/DataBase/Querys.java tools/material-usuario/GenerarMaterial.java
if($LASTEXITCODE -ne 0){throw 'Falló la compilación.'}
$prevUrl=$env:PDV_DB_URL;$prevUser=$env:PDV_DB_USER;$prevPassword=$env:PDV_DB_PASSWORD
try{
 $env:PDV_DB_URL='jdbc:hsqldb:mem:material_usuario';$env:PDV_DB_USER='SA';$env:PDV_DB_PASSWORD='demo'
 $arguments=@('-Xmx1g','-cp',('build/material-tools/classes;'+$cp),'GenerarMaterial')
 if($SoloManual){$arguments+='manual'} elseif($SoloVideo){$arguments+='video'}
 & (Join-Path $JdkHome 'bin/java.exe') @arguments
 if($LASTEXITCODE -ne 0){throw 'Falló la generación del material.'}
}finally{$env:PDV_DB_URL=$prevUrl;$env:PDV_DB_USER=$prevUser;$env:PDV_DB_PASSWORD=$prevPassword}
