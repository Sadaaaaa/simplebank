{{- define "transfer.name" -}}
{{- include "microservice.name" . }}
{{- end }}

{{- define "transfer.fullname" -}}
{{- include "microservice.fullname" . }}
{{- end }}

{{- define "transfer.chart" -}}
{{- include "microservice.chart" . }}
{{- end }}

{{- define "transfer.labels" -}}
{{- include "microservice.labels" . }}
{{- end }}

{{- define "transfer.selectorLabels" -}}
{{- include "microservice.selectorLabels" . }}
{{- end }}
