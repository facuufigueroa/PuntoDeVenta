param([string]$JdkHome = $env:JAVA_HOME)
$ErrorActionPreference = 'Stop'
$raizProyecto = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
Set-Location -LiteralPath $raizProyecto
if (-not $JdkHome) {
    $compilador = Get-Command javac -ErrorAction Stop
    $JdkHome = Split-Path (Split-Path $compilador.Source -Parent) -Parent
    if (-not (Test-Path -LiteralPath (Join-Path $JdkHome 'bin/jar.exe'))) {
        $jdkEncontrado = Get-ChildItem -LiteralPath (Join-Path $env:ProgramFiles 'Java') -Directory | Where-Object { Test-Path -LiteralPath (Join-Path $_.FullName 'bin/jar.exe') } | Sort-Object Name -Descending | Select-Object -First 1
        if ($jdkEncontrado) { $JdkHome = $jdkEncontrado.FullName }
    }
}
$javac = Join-Path $JdkHome 'bin/javac.exe'
$java = Join-Path $JdkHome 'bin/java.exe'
$jar = Join-Path $JdkHome 'bin/jar.exe'
foreach ($tool in @($javac,$java,$jar)) { if (-not (Test-Path -LiteralPath $tool)) { throw 'Indicá un JDK 17 o posterior mediante -JdkHome.' } }
$bibliotecas = @(Get-Content -LiteralPath 'tools/runtime-libs.txt')
foreach ($biblioteca in $bibliotecas) { if (-not (Test-Path -LiteralPath (Join-Path 'lib' $biblioteca))) { throw "Falta lib/$biblioteca" } }
$nombrePaquete = 'PuntoDeVenta-' + (Get-Date -Format 'yyyyMMdd-HHmmss')
$carpetaClases = Join-Path 'build' $nombrePaquete
$carpetaPaquete = Join-Path 'dist' $nombrePaquete
New-Item -ItemType Directory -Force -Path $carpetaClases,$carpetaPaquete,(Join-Path $carpetaPaquete 'lib'),(Join-Path $carpetaPaquete 'config'),(Join-Path $carpetaPaquete 'database'),(Join-Path $carpetaPaquete 'docs') | Out-Null
$classpath = ($bibliotecas | ForEach-Object { Join-Path 'lib' $_ }) -join ';'
$fuentes = @(Get-ChildItem -LiteralPath 'src' -Filter '*.java' -Recurse | ForEach-Object FullName)
& $javac -encoding UTF-8 --release 8 -cp $classpath -d $carpetaClases $fuentes 'tools/PrepararRecursos.java' 'tools/CrearManual.java'
if ($LASTEXITCODE -ne 0) { throw 'Falló la compilación.' }
& $java '-Djava.awt.headless=true' -cp "$carpetaClases;$classpath" PrepararRecursos
if ($LASTEXITCODE -ne 0) { throw 'Falló la preparación de recursos.' }
& $java '-Djava.awt.headless=true' -cp "$carpetaClases;$classpath" CrearManual
if ($LASTEXITCODE -ne 0) { throw 'Falló la generación del manual.' }
Copy-Item -LiteralPath 'src/Imagenes' -Destination $carpetaClases -Recurse
New-Item -ItemType Directory -Force -Path (Join-Path $carpetaClases 'Reporte') | Out-Null
Copy-Item -LiteralPath 'src/Reporte/ticket.jasper','src/Reporte/ventas.jasper' -Destination (Join-Path $carpetaClases 'Reporte')
# Use an allowlist: no private configuration, dumps, backups, real catalogs or test data.
foreach ($biblioteca in $bibliotecas) { Copy-Item -LiteralPath (Join-Path 'lib' $biblioteca) -Destination (Join-Path $carpetaPaquete 'lib') }
Copy-Item -LiteralPath 'lib/licencias','lib/sources' -Destination (Join-Path $carpetaPaquete 'lib') -Recurse
Copy-Item -LiteralPath 'config/database.properties.example','config/empresa.properties.example' -Destination (Join-Path $carpetaPaquete 'config')
Get-ChildItem -LiteralPath 'database' -File -Filter '*.sql' | Copy-Item -Destination (Join-Path $carpetaPaquete 'database')
Get-ChildItem -LiteralPath 'docs' -File -Filter '*.md' | Copy-Item -Destination (Join-Path $carpetaPaquete 'docs')
Copy-Item -LiteralPath 'docs/Manual-de-usuario.pdf','docs/Manual-de-usuario.html' -Destination (Join-Path $carpetaPaquete 'docs')
Copy-Item -LiteralPath 'distribution/INICIAR.cmd','distribution/INSTALACION.txt' -Destination $carpetaPaquete
$manifest = Join-Path 'build' ($nombrePaquete + '-MANIFEST.MF')
$rutaLib = ($bibliotecas | ForEach-Object { 'lib/' + $_ }) -join ' '
$linea = "Class-Path: $rutaLib"
$lineas = @()
while ($linea.Length -gt 70) { $lineas += $linea.Substring(0,70); $linea = " " + $linea.Substring(70) }
$lineas += $linea
[IO.File]::WriteAllText((Join-Path $raizProyecto $manifest),"Manifest-Version: 1.0`r`nMain-Class: puntodeventa.Main`r`n" + ($lineas -join "`r`n") + "`r`n`r`n",[Text.Encoding]::ASCII)
& $jar cfm (Join-Path $carpetaPaquete 'PuntoDeVenta.jar') $manifest -C $carpetaClases .
if ($LASTEXITCODE -ne 0) { throw 'Falló la creación del JAR.' }
$zip = Join-Path 'dist' ($nombrePaquete + '.zip')
Compress-Archive -LiteralPath $carpetaPaquete -DestinationPath $zip
Write-Output "Paquete listo: $zip"
