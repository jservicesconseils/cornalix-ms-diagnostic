variable "project" {
  type    = string
  default = "cornalix"
}

variable "environment" {
  type    = string
  default = "prod"
}

variable "aws_region" {
  type    = string
  default = "ca-central-1"
}

variable "service_name" {
  type    = string
  default = "diagnostic"
}

variable "container_port" {
  type    = number
  default = 8082
}

variable "image_tag" {
  type    = string
  default = "latest"
}

variable "public_hostname" {
  type    = string
  default = "diagnostic.api.cornalix.ca"
}

variable "alb_rule_priority" {
  type    = number
  default = 102
}
