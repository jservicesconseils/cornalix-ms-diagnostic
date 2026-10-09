output "target_group_arn" {
  value = aws_lb_target_group.diagnostic.arn
}

output "ecs_service_name" {
  value = aws_ecs_service.diagnostic.name
}
