import React from "react";
import { SettingsApplications } from "./SettingsApplications";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SettingsApplications",
  component: SettingsApplications,
};

export const Default = () => <SettingsApplications />;
export const Fill = () => <SettingsApplications fill="blue" />;
export const Size = () => <SettingsApplications height="50" width="50" />;
export const CustomStyle = () => <SettingsApplications style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SettingsApplications className="custom-class" />;
