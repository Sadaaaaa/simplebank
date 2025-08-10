{{- define "exchange.name" -}}
{{- include "microservice.name" . }}
{{- end }}

{{- define "exchange.fullname" -}}
{{- include "microservice.fullname" . }}
{{- end }}

{{- define "exchange.chart" -}}
{{- include "microservice.chart" . }}
{{- end }}

{{- define "exchange.labels" -}}
{{- include "microservice.labels" . }}
{{- end }}

{{- define "exchange.selectorLabels" -}}
{{- include "microservice.selectorLabels" . }}
{{- end }}
