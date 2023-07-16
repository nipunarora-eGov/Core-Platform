import React from "react";
import { SMSText } from "./SMSText";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SMSText",
  component: SMSText,
};

export const Default = () => <SMSText />;
export const Fill = () => <SMSText fill="blue" />;
export const Size = () => <SMSText height="50" width="50" />;
export const CustomStyle = () => <SMSText style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SMSText className="custom-class" />;
