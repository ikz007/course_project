CREATE TABLE IF NOT EXISTS ScenarioSetting (
    SettingName VARCHAR(50) NOT NULL,
    SettingSQL VARCHAR(2000) NOT NULL
);

CREATE UNIQUE INDEX ScenarioSetting_Name
ON ScenarioSetting(SettingName);