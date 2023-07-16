import React from "react";
import { ExitToApp } from "./ExitToApp";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ExitToApp",
  component: ExitToApp,
};

export const Default = () => <ExitToApp />;
export const Fill = () => <ExitToApp fill="blue" />;
export const Size = () => <ExitToApp height="50" width="50" />;
export const CustomStyle = () => <ExitToApp style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ExitToApp className="custom-class" />;
