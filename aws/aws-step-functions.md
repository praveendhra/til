# AWS Step Functions

## What Is It?
Serverless workflow orchestration using state machines defined in ASL (Amazon States Language).

## State Types
| State | Purpose |
|-------|---------|
| Task | Execute work (Lambda, ECS, API) |
| Choice | Branch based on condition |
| Parallel | Execute branches concurrently |
| Map | Iterate over array |
| Wait | Delay execution |
| Pass | Pass input to output |
| Succeed/Fail | Terminal states |

## Example Workflow
```json
{
  "StartAt": "ValidateOrder",
  "States": {
    "ValidateOrder": {
      "Type": "Task",
      "Resource": "arn:aws:lambda:...:validate",
      "Next": "CheckInventory",
      "Retry": [{"ErrorEquals": ["ServiceException"], "MaxAttempts": 3}]
    },
    "CheckInventory": {
      "Type": "Choice",
      "Choices": [
        {"Variable": "$.inStock", "BooleanEquals": true, "Next": "ProcessPayment"}
      ],
      "Default": "OutOfStock"
    },
    "ProcessPayment": {
      "Type": "Task",
      "Resource": "arn:aws:lambda:...:payment",
      "Next": "SendConfirmation"
    },
    "SendConfirmation": {
      "Type": "Task",
      "Resource": "arn:aws:lambda:...:notify",
      "End": true
    }
  }
}
```

## Standard vs Express
| Feature | Standard | Express |
|---------|----------|---------|
| Duration | Up to 1 year | Up to 5 minutes |
| Pricing | Per state transition | Per execution |
| Execution history | Yes | CloudWatch only |
| Use case | Long-running | High-volume |
