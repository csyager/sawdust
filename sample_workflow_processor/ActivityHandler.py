import json

class Activity():
    def __init__(self, s: str):
        json_obj = json.loads(s)
        self.activity_id = json_obj["activityId"]
        self.workflow_id = json_obj["workflowId"]
        self.workflow_state = json_obj["workflowState"]
        self.activity_state = json_obj["activityState"]

class ActivityHandler():
    def handle_STARTING_activity(self, request: str):
        activity = Activity(request)
        print(activity.activity_id)
        print(activity.workflow_id)
        print(activity.workflow_state)
        print(activity.activity_state)