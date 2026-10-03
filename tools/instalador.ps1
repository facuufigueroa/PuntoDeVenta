param([string]$Paquete,[string]$Makensis="build/installer-tools/nsis-3.11/makensis.exe")
$ErrorActionPreference='Stop'
$raiz=[IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
Set-Location -LiteralPath $raiz
if(-not $Paquete){$Paquete=(Get-ChildItem -LiteralPath dist -Directory -Filter 'PuntoDeVenta-*'|Sort-Object Name -Descending|Select-Object -First 1).FullName}
$Paquete=[IO.Path]::GetFullPath($Paquete)
if(-not (Test-Path -LiteralPath (Join-Path $Paquete 'PuntoDeVenta.jar'))){throw 'Generá primero el paquete con tools/empaquetar.ps1.'}
$compiler=[IO.Path]::GetFullPath($Makensis)
if(-not(Test-Path -LiteralPath $compiler)){throw 'Indicá el compilador NSIS mediante -Makensis.'}
$limpieza=Join-Path $raiz 'build/desinstalar-archivos.nsh'
$lineas=@()
foreach($f in Get-ChildItem -LiteralPath $Paquete -File -Recurse){$rel=$f.FullName.Substring($Paquete.Length+1);if(-not $rel.StartsWith('config\')){$lineas+='Delete "$INSTDIR\'+$rel+'"'}}
foreach($d in Get-ChildItem -LiteralPath $Paquete -Directory -Recurse|Sort-Object {$_.FullName.Length} -Descending){$rel=$d.FullName.Substring($Paquete.Length+1);if(-not $rel.StartsWith('config')){$lineas+='RMDir "$INSTDIR\'+$rel+'"'}}
[IO.File]::WriteAllLines($limpieza,$lineas,(New-Object Text.UTF8Encoding($true)))
$salida=Join-Path $raiz ('dist/Instalar-'+(Split-Path $Paquete -Leaf)+'.exe')
& $compiler '/V2' "/DPAQUETE=$Paquete" "/DSALIDA=$salida" "/DLIMPIEZA=$limpieza" (Join-Path $raiz 'distribution/instalador.nsi')
if($LASTEXITCODE -ne 0){throw 'Falló la creación del instalador.'}
Write-Output "Instalador listo: $salida"
