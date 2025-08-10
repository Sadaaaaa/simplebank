{{- define "notifications.name" -}}
{{- include "microservice.name" . }}
{{- end }}

{{- define "notifications.fullname" -}}
{{- include "microservice.fullname" . }}
{{- end }}

{{- define "notifications.chart" -}}
{{- include "microservice.chart" . }}
{{- end }}

{{- define "notifications.labels" -}}
{{- include "microservice.labels" . }}
{{- end }}

{{- define "notifications.selectorLabels" -}}
{{- include "microservice.selectorLabels" . }}
{{- end }}
