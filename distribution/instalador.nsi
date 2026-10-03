Unicode true
!include "MUI2.nsh"
!include "FileFunc.nsh"
Name "Punto de Venta"
OutFile "${SALIDA}"
InstallDir "$LOCALAPPDATA\PuntoDeVenta"
InstallDirRegKey HKCU "Software\PuntoDeVenta" "InstallDir"
RequestExecutionLevel user
SetCompressor /SOLID lzma
ShowInstDetails show
ShowUninstDetails show
!define MUI_ABORTWARNING
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!define MUI_FINISHPAGE_TEXT "Instalación completa. Necesitás Java 8 o posterior y MySQL 8. Consultá INSTALACION.txt y prepará la base antes del primer inicio."
!insertmacro MUI_PAGE_FINISH
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES
!insertmacro MUI_LANGUAGE "Spanish"
Section "Programa"
  SetOutPath "$INSTDIR"
  File /r "${PAQUETE}\*.*"
  WriteUninstaller "$INSTDIR\Desinstalar.exe"
  ${GetParameters} $R0
  ClearErrors
  ${GetOptions} $R0 "/TEST" $R1
  IfErrors +2
  Goto fin_registro
  CreateDirectory "$SMPROGRAMS\Punto de Venta"
  CreateShortCut "$SMPROGRAMS\Punto de Venta\Punto de Venta.lnk" "$INSTDIR\INICIAR.cmd"
  CreateShortCut "$SMPROGRAMS\Punto de Venta\Instrucciones.lnk" "$INSTDIR\INSTALACION.txt"
  CreateShortCut "$SMPROGRAMS\Punto de Venta\Manual de usuario.lnk" "$INSTDIR\docs\Manual-de-usuario.pdf"
  CreateShortCut "$SMPROGRAMS\Punto de Venta\Desinstalar.lnk" "$INSTDIR\Desinstalar.exe"
  CreateShortCut "$DESKTOP\Punto de Venta.lnk" "$INSTDIR\INICIAR.cmd"
  WriteRegStr HKCU "Software\PuntoDeVenta" "InstallDir" "$INSTDIR"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\PuntoDeVenta" "DisplayName" "Punto de Venta"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\PuntoDeVenta" "UninstallString" '$\"$INSTDIR\Desinstalar.exe$\"'
  WriteRegDWORD HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\PuntoDeVenta" "NoModify" 1
  WriteRegDWORD HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\PuntoDeVenta" "NoRepair" 1
  fin_registro:
SectionEnd
Section "Uninstall"
  !include "${LIMPIEZA}"
  Delete "$INSTDIR\Desinstalar.exe"
  ${GetParameters} $R0
  ClearErrors
  ${GetOptions} $R0 "/TEST" $R1
  IfErrors +2
  Goto fin_registro
  Delete "$DESKTOP\Punto de Venta.lnk"
  Delete "$SMPROGRAMS\Punto de Venta\Punto de Venta.lnk"
  Delete "$SMPROGRAMS\Punto de Venta\Instrucciones.lnk"
  Delete "$SMPROGRAMS\Punto de Venta\Manual de usuario.lnk"
  Delete "$SMPROGRAMS\Punto de Venta\Desinstalar.lnk"
  RMDir "$SMPROGRAMS\Punto de Venta"
  RMDir "$INSTDIR"
  DeleteRegKey HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\PuntoDeVenta"
  DeleteRegKey HKCU "Software\PuntoDeVenta"
  fin_registro:
SectionEnd
